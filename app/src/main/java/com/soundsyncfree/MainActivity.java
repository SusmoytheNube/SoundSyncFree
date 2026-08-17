package com.soundsyncfree;

import android.app.*;import android.os.*;import android.content.*;import android.database.Cursor;import android.net.Uri;import android.provider.OpenableColumns;import android.media.MediaPlayer;import android.view.*;import android.widget.*;import java.io.*;import java.net.*;import java.util.*;import java.util.concurrent.*;

public class MainActivity extends Activity {
    static final int PICK=42, UDP_PORT=18081, HTTP_PORT=18080;
    TextView status; ArrayAdapter<String> adapter; ArrayList<Host> hosts=new ArrayList<>();
    Uri selected; File selectedFile; LocalHttpServer server; Thread discovery; volatile boolean running=true; MediaPlayer player;
    Handler h=new Handler(Looper.getMainLooper());

    static class Host { String name; InetAddress addr; int port; Host(String n,InetAddress a,int p){name=n;addr=a;port=p;} public String toString(){return name+"  ("+addr.getHostAddress()+")";} }

    @Override public void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_main);
        status=findViewById(R.id.status); ListView list=findViewById(R.id.hosts);
        adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,new ArrayList<String>());list.setAdapter(adapter);
        findViewById(R.id.select).setOnClickListener(v->pick());
        findViewById(R.id.startHost).setOnClickListener(v->startHost());
        findViewById(R.id.stopHost).setOnClickListener(v->stopHost());
        findViewById(R.id.youtube).setOnClickListener(v->{ Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.youtube.com/"));startActivity(i); });
        list.setOnItemClickListener((p,v,pos,id)->connect(hosts.get(pos)));
        startDiscovery();
    }
    void pick(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("audio/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,PICK);}
    @Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(r==PICK&&c==RESULT_OK&&d!=null){selected=d.getData();try{selectedFile=copyToCache(selected);status.setText("Selected: "+selectedFile.getName());}catch(Exception e){status.setText("Select failed: "+e.getMessage());}}}
    File copyToCache(Uri u)throws Exception{String name="track.bin";Cursor c=getContentResolver().query(u,null,null,null,null);if(c!=null){int ix=c.getColumnIndex(OpenableColumns.DISPLAY_NAME);if(c.moveToFirst()&&ix>=0)name=c.getString(ix);c.close();}File f=new File(getCacheDir(),"soundsync_"+name.replaceAll("[^A-Za-z0-9._-]","_"));try(InputStream in=getContentResolver().openInputStream(u);OutputStream out=new FileOutputStream(f)){byte[] buf=new byte[65536];int n;while((n=in.read(buf))>0)out.write(buf,0,n);}return f;}
    void startHost(){if(selectedFile==null){status.setText("Select a local audio file first");return;}try{stopHost();server=new LocalHttpServer(selectedFile,HTTP_PORT);server.start();status.setText("Hosting "+selectedFile.getName()+" — no device-count cap");broadcast("HOST|SoundSync Host|"+HTTP_PORT);new Thread(()->{try{while(server!=null){Thread.sleep(1500);broadcast("HOST|SoundSync Host|"+HTTP_PORT);}}catch(Exception ignored){}}).start();}catch(Exception e){status.setText("Host failed: "+e.getMessage());}}
    void stopHost(){if(server!=null){server.stop();server=null;} }
    void connect(Host x){send("REQUEST",x.addr);status.setText("Request sent to "+x.name);}
    String selectedFileName(){return selectedFile==null?"track.bin":selectedFile.getName();}
    void send(String s,InetAddress a){new Thread(()->{try(DatagramSocket ds=new DatagramSocket()){byte[] b=s.getBytes("UTF-8");ds.send(new DatagramPacket(b,b.length,a,UDP_PORT));}catch(Exception ignored){}}).start();}
    void broadcast(String s){new Thread(()->{try{DatagramSocket ds=new DatagramSocket();ds.setBroadcast(true);byte[] b=s.getBytes("UTF-8");ds.send(new DatagramPacket(b,b.length,InetAddress.getByName("255.255.255.255"),UDP_PORT));ds.close();}catch(Exception ignored){}}).start();}
    void startDiscovery(){discovery=new Thread(()->{try{DatagramSocket ds=new DatagramSocket(UDP_PORT);ds.setBroadcast(true);ds.setSoTimeout(1000);byte[] buf=new byte[2048];while(running){try{DatagramPacket p=new DatagramPacket(buf,buf.length);ds.receive(p);String s=new String(p.getData(),0,p.getLength(),"UTF-8");String[] a=s.split("\\|",4);if(a[0].equals("REQUEST") && server!=null){ long start=System.currentTimeMillis()+3000; send("CTRL|"+HTTP_PORT+"|"+start+"|"+selectedFileName(),p.getAddress()); } else if(a[0].equals("HOST")){Host found=new Host(a[1],p.getAddress(),Integer.parseInt(a[2]));boolean exists=false;for(Host z:hosts)if(z.addr.equals(found.addr))exists=true;if(!exists){hosts.add(found);h.post(()->{adapter.add(found.toString());adapter.notifyDataSetChanged();});}}else if(a[0].equals("CTRL")){int port=Integer.parseInt(a[1]);long at=Long.parseLong(a[2]);playStream(p.getAddress(),port,at);}}catch(SocketTimeoutException ignored){}}ds.close();}catch(Exception e){h.post(()->status.setText("Discovery error: "+e.getMessage()));}});discovery.start();}
    void playStream(InetAddress addr,int port,long startAt){h.post(()->status.setText("Preparing synchronized stream…"));try{if(player!=null){player.release();player=null;}player=new MediaPlayer();player.setDataSource("http://"+addr.getHostAddress()+":"+port+"/audio");player.setOnPreparedListener(mp->{long delay=Math.max(0,startAt-System.currentTimeMillis());h.postDelayed(()->{if(player!=null)player.start();},delay);});player.setOnErrorListener((mp,w,e)->{h.post(()->status.setText("Playback error "+w+"/"+e));return true;});player.prepareAsync();}catch(Exception e){h.post(()->status.setText("Stream error: "+e.getMessage()));}}
    @Override protected void onDestroy(){running=false;stopHost();if(player!=null)player.release();super.onDestroy();}
}
