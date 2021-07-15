import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MyHttpServer {
    public static void main(String[] args) throws Exception{
        ServerSocket serverSocket = new ServerSocket(80);
        System.out.println("服务器已启动，监听端口80 。。。");
        while (true){   //来了请求就处理
            final Socket socket = serverSocket.accept();
            //获取输入流
            final  InputStream is = socket.getInputStream();
            //获取输出流
            final OutputStream os = socket.getOutputStream();
            final byte[] buf = new byte[1024*1024];
            new Thread(new Runnable() {
                public void run() {
                    //读服务器传过来的信息 request
                    int len = 0;
                    try {
                        len = is.read(buf);
                        if (len == -1) {
                            return;
                        }
                        //System.out.println("读到字节数：" + len);
                        String hexMsg = bytes2Hexs(buf, len);
                        //System.out.println(hexMsg);

                        String msg = new String(buf, 0, len);
                        //System.out.println(msg);

                        String[] split = msg.split("\r\n");
                        //解析第一行
                        // GET /favicon.ico HTTP/1.1
                        String[] url = split[0].split(" ");
                        String path = url[1].split("[?]")[0];
                        System.out.println(path);
                        //接收参数 集合
                        List<Integer> list = new ArrayList<Integer>();
                        //参数  url[1].split("[?]")[1]
                        if (!(url[1].split("[?]").length < 2)){
                            String params  = url[1].split("[?]")[1];
                            //参数
                            String[] param2 = params.split("&");
                            for(String s : param2){
                                String param = s.split("=")[1];
                                list.add(Integer.valueOf(param));
                            }
                        }
                        //?name=zhangsan&pasww=13
                        //执行
                        int result = parsePath(path, list);
                        //回应  response
                        os.write("HTTP/1.1 200 OK\r\n".getBytes()); //使用HTTP协议 1.1版本 状态码200 正确 回车换行
                        os.write("Content-Type: text/plain\r\n".getBytes());//内容类型是普通文本 回车换行
                        os.write("\r\n".getBytes());// 回车换行
                        os.write(("" + result).getBytes()); //返回给浏览器的文本内容
                        os.flush(); //主动刷新

                    } catch (IOException e) {
                        e.printStackTrace();
                    }finally {
                       if (socket != null){
                           try {
                               socket.close();
                           } catch (IOException e) {
                               e.printStackTrace();
                           }
                       }
                    }
                }
            }).start();
        }
        
    }

    /**
     * 解析路径
     */
    public static int parsePath(String path , List<Integer> list){
        int result;
        if (path.equals("/add")){
            int sum = 0;
            for(Integer data : list){
                sum += data;
            }
            result = sum;
        }else if(path.equals("/mult")){
            int sum = 1;
            for(Integer data : list){
                sum *= data;
            }
            result = sum;
        }else{
            result = 0;
        }
        return result;
    }

    /*
    * 将bytes -》 String
    * */
    public static String bytes2Hexs(byte[] buf , int len){
        StringBuffer sb  = new StringBuffer();
        StringBuffer sb1  = new StringBuffer();
        StringBuffer sb2  = new StringBuffer();
        int count = 0;
        for (int i = 0; i < len; i++) {
            byte bt = buf[i];
            sb1.append(byte2Hex(bt) + " ");
            if(buf[i] >= 0x20 && buf[i] <= 0x7e){
                sb2.append((char)buf[i]);
            }else{
                sb2.append(".");
            }
            count++;
            if (count % 8 == 0){
                sb1.append(" ");
            }
            if (count % 16 == 0){
                sb.append(sb1).append(sb2).append("\r\n");
                sb1 = new StringBuffer();
                sb2 = new StringBuffer();
                count = 0;
            }
        }
        if (count != 0){
            sb.append(sb1).append("  ").append(sb2).append("\r\n");
        }
        return sb.toString();
    }

    /*
    * byte  -> 十六进制
    *
    * */
    public static String byte2Hex(byte b){
        String s = Integer.toHexString(b).toUpperCase();
        if (s.length() < 2){
            return 0 + s;
        }
        return s;
    }


}
