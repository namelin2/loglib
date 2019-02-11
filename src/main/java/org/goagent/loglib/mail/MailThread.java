package org.goagent.loglib.mail;

import org.goagent.loglib.callback.MailThreadStateListener;

/**************************************
 * MailThread
 * company：setmath(数韬)
 * author:Arlen
 * version:v1.0
 * desc:
 * create date:2018/6/21 17:19
 * history desc: 
 * recent version:v1.0
 * version desc:
 * modified author:
 * modified date:
 * modified phone：
 **************************************/
public class MailThread extends Thread
{
    private String sender;
    private String senderPassword;
    private String receiver;
    private String subject;
    private String content;
    private MailThreadStateListener mMailThreadStateListener;
    public MailThread(String subject, String content,MailThreadStateListener mMailThreadStateListener)
    {
        this.subject = subject;
        this.content = content;
        this.mMailThreadStateListener=mMailThreadStateListener;
    }

    public MailThread(String sender, String senderPassword, String receiver, String subject, String content,MailThreadStateListener mMailThreadStateListener)
    {
        this.sender = sender;
        this.senderPassword = senderPassword;
        this.receiver = receiver;
        this.subject = subject;
        this.content = content;
        this.mMailThreadStateListener=mMailThreadStateListener;
    }

    @Override
    public void run()
    {
        super.run();
        System.out.println("开始发送邮件...");
        MailUtils.sendMail(sender, senderPassword, receiver, subject, content,mMailThreadStateListener);
        System.out.println("发送邮件完成...");
    }

}
