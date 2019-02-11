package org.goagent.loglib.mail;

import android.text.TextUtils;
import android.util.Base64;

import org.goagent.loglib.callback.MailThreadStateListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**************************************
 * MailUtils  邮件发送工具类
 * company：setmath(数韬)
 * author:Arlen
 * version:v1.0
 * desc:
 * 1.用于发送邮件，用途为用户反馈，崩溃日志提交以及其他信息传递
 *
 * *************************
 *   邮件服务返回代码含义
 *   500   格式错误，命令不可识别（此错误也包括命令行过长）
 *   501   参数格式错误
 *   502   命令不可实现
 *   503   错误的命令序列
 *   504   命令参数不可实现
 *   211   系统状态或系统帮助响应
 *   214   帮助信息
 *   220   服务就绪
 *   221   服务关闭传输信道
 *   421   服务未就绪，关闭传输信道（当必须关闭时，此应答可以作为对任何命令的响应）
 *   250   要求的邮件操作完成
 *   251   用户非本地，将转发向
 *   450   要求的邮件操作未完成，邮箱不可用（例如，邮箱忙）
 *   550   要求的邮件操作未完成，邮箱不可用（例如，邮箱未找到，或不可访问）
 *   451   放弃要求的操作；处理过程中出错
 *   551   用户非本地，请尝试
 *   452   系统存储不足，要求的操作未执行
 *   552   过量的存储分配，要求的操作未执行
 *   553   邮箱名不可用，要求的操作未执行（例如邮箱格式错误）
 *   354   开始邮件输入，以.结束
 *   554   操作失败
 *   535   用户验证失败
 *   235   用户验证成功
 *   334   等待用户输入验证信息
 *   *************************
 *
 * create date:2018/6/21 16:17
 * history desc: 
 * recent version:v1.0
 * version desc:
 * modified author:
 * modified date:
 * modified phone：
 **************************************/
final class MailUtils
{
    private static final String EMAIL_HOST = "smtp.126.com";//邮件服务器网关  不同的邮箱使用不同的邮箱服务器  163邮箱：smtp.163.com  126邮箱：smtp.126.com
    private static final int EMAIL_PORT = 25;//端口号
    private static final String DEFAULT_SENDER = "namelin2@126.com";//默认发件人
    private static final String DEFAULT_SENDER_PASSWORD = "apperrlog2018";//默认发件人密码
    private static final String DEFAULT_RECEIVER = "namelin2@qq.com";//默认收件人
    private static final String HOLE_COMMAND = "HELO 126.com";//使用标准的SMTP，向服务器标识用户身份。发送者能进行欺骗，但一般情况下服务器都能检测到
    private static final String AUTH_LOGIN = "auth login";
    private static final String PREFIX_SENDER = "mail from:<";//命令中指定的地址是发件人地址
    private static final String SUFFIX_SENDER = ">";
    private static final String PREFIX_RECEIVER = "rcpt to:<";//标识单个的邮件接收人；可有多个 RCPT TO；常在 MAIL 命令后面
    private static final String SUFFIX_RECEIVER = ">";
    private static final String SEND_DATA_START = "data";//在单个或多个 RCPT 命令后，表示所有的邮件接收人已标识，并初始化数据传输，以CRLF.CRLF 结束
    private static final String SUBJECT = "subject:";//主题
    private static final String FROM = "from:";//发件人
    private static final String TO = "to:";//收件人
    private static final String CONTENT_TYPE_TEXT_PLAIN = "Content-Type: text/plain;charset=\"UTF-8\"";//文本编码格式
    private static final String END_FLAG = ".";//结束标记
    private static final String RSET = "rset";//重置会话，当前传输被取消，服务器响应 250 OK
    private static final String QUIT = "quit";//结束会话

    private MailUtils()
    {
    }

    /**
     * 发送邮件
     *
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    public static void sendMail(final String subject, final String content)
    {
        sendMail(null, null, null, subject, content, null);
    }

    /**
     * 发送邮件
     *
     * @param sender         发件人邮箱
     * @param senderPassword 发件人密码
     * @param receiver       收件人邮箱
     * @param subject        邮件主题
     * @param content        邮件内容
     * @param callback       邮件发送状态回调
     */
    public static void sendMail(String sender, String senderPassword, String receiver, String subject, String content, MailThreadStateListener callback)
    {
        if (TextUtils.isEmpty(sender))
        {
            sender = DEFAULT_SENDER;
        }
        if (TextUtils.isEmpty(senderPassword))
        {
            senderPassword = DEFAULT_SENDER_PASSWORD;
        }
        if (TextUtils.isEmpty(receiver))
        {
            receiver = DEFAULT_RECEIVER;
        }
        //上文说过，这个用户名和密码是要使用base64进行加密的，加密方法见下文附录1详解
        String user = base64Encode2String(sender.substring(0, sender.indexOf("@")).getBytes());
        ;  //截取出用户名并加密
        String pass = base64Encode2String(senderPassword.getBytes());//加密 用户密码
        Socket socket = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        BufferedReader reader = null;
        PrintWriter writter = null;
        try
        {
            //建立Socket连接:
            socket = new Socket(EMAIL_HOST, EMAIL_PORT);  //smtp服务使用25号端口监听
            //获取该socket的输入输出流
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            writter = new PrintWriter(outputStream, true);  //这个true太关键了，我刚才没写这个别坑了!你可以不加这个试下效果，下文附录2会写到为什么加true
            //HELO  发送HELO信息
            writter.println(HOLE_COMMAND);

            System.out.println(reader.readLine());

            //AUTH LOGIN  发送AUTH LOGIN信息
            writter.println(AUTH_LOGIN);
            System.out.println(reader.readLine());

            writter.println(user);
            System.out.println(user);

            System.out.println(reader.readLine());

            writter.println(pass);
            System.out.println(pass);

            System.out.println(reader.readLine());

            //Above   Authentication successful
            //Set mail from   and   rcpt to   发送发件人和收件人信息
            writter.println(PREFIX_SENDER + sender + SUFFIX_SENDER);
            System.out.println(reader.readLine());

            writter.println(PREFIX_RECEIVER + receiver + SUFFIX_RECEIVER);
            System.out.println(reader.readLine());

            //Set data  告诉服务器我要传数据
            writter.println(SEND_DATA_START);
            System.out.println(reader.readLine());

            //发邮件主题，收件人，发件人，正文
            writter.println(SUBJECT + subject);//主题
            writter.println(FROM + sender);//发件人
            writter.println(TO + receiver);//收件人
            writter.println(CONTENT_TYPE_TEXT_PLAIN);//如果发送正文必须加这个，而且下面要有一个空行

            writter.println();
            writter.println(content);
            writter.println(END_FLAG);//告诉服务器我发送的内容完毕了
            writter.println("");
            System.out.println(reader.readLine());

            //退出邮件服务器
            writter.println(RSET);
            System.out.println(reader.readLine());

            writter.println(QUIT);
            System.out.println(reader.readLine());
            if (null != callback)
            {
                callback.sendSuccess();
            }
        } catch (IOException e)
        {
            e.printStackTrace();
            if (null != callback)
            {
                callback.sendFail();
            }
        } finally
        {
            if (null != writter)
            {
                writter.close();
            }
            if (null != reader)
            {
                try
                {
                    reader.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            if (null != outputStream)
            {
                try
                {
                    outputStream.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            if (null != inputStream)
            {
                try
                {
                    inputStream.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            if (null != socket)
            {
                try
                {
                    socket.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Base64编码
     *
     * @param input 要编码的字节数组
     * @return Base64编码后的字符串
     */
    private static String base64Encode2String(byte[] input)
    {
        return Base64.encodeToString(input, Base64.NO_WRAP);
    }
}
