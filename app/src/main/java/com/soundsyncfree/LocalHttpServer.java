package com.soundsyncfree;
import java.io.*;import java.net.*;import java.util.concurrent.*;
public class LocalHttpServer {
 private final File file; private final int port; private volatile boolean running; private ServerSocket server;
 public LocalHttpServer(File f,int p){file=f;port=p;}
 public void start()throws IOException{server=new ServerSocket(port);running=true;Thread t=new Thread(()->{while(running){try{Socket s=server.accept();new Thread(()->serve(s)).start();}catch(IOException ignored){}}});t.start();}
 void serve(Socket s){try{BufferedReader r=new BufferedReader(new InputStreamReader(s.getInputStream()));String line;while((line=r.readLine())!=null&&!line.isEmpty()){}OutputStream out=s.getOutputStream();String hdr="HTTP/1.1 200 OK\r\nContent-Type: audio/mpeg\r\nContent-Length: "+file.length()+"\r\nConnection: close\r\n\r\n";out.write(hdr.getBytes("UTF-8"));try(InputStream in=new BufferedInputStream(new FileInputStream(file))){byte[] b=new byte[65536];int n;while((n=in.read(b))!=-1){out.write(b,0,n);out.flush();}}s.close();}catch(Exception ignored){try{s.close();}catch(Exception ignored2){}}}
 public void stop(){running=false;try{server.close();}catch(Exception ignored){}}
}
