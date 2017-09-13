package se.acrend.ppm.mail

import com.sendgrid.Content
import com.sendgrid.Email
import com.sendgrid.Mail
import com.sendgrid.Method
import com.sendgrid.Request
import com.sendgrid.Response
import com.sendgrid.SendGrid
import org.apache.commons.io.IOUtils
import org.rythmengine.RythmEngine
import org.springframework.stereotype.Component
import se.acrend.ppm.domain.FundInfo

import java.io.IOException
import java.util.HashMap
import java.util.Properties


/**
 *
 */
@Component
class FundMailer {

    private val engine: RythmEngine

    init {
        val conf = HashMap<String, Any>()
        conf.put("engine.file_write.enabled", false)
        conf.put("engine.gae.enabled", true)

        engine = RythmEngine(conf)
    }

    @Throws(Exception::class)
    fun sendMail(funds: List<FundInfo>) {


        val key = System.getProperty("sendgrid.api.key")
        val sendGrid = SendGrid(key)


        val from = Email("mailer@ppmselector.appspotmail.com", "PPM-väljaren")
        val subject = "Nytt val för PPM-fonder!"
        val to = Email("johan.kindgren@gmail.com")
//        val to = Email("ppmselector@googlegroups.com")
        val htmlBody = createHtmlMessage(funds)
        val mail = Mail(from, subject, to, Content("text/plain", "Dags att byta fonder igen!"))
        mail.addContent(Content("text/html", htmlBody))

        val request = Request()
        try {
            request.method = Method.POST
            request.endpoint = "mail/send"
            request.body = mail.build()
            sendGrid.api(request)
        } catch (ex: IOException) {
            throw ex
        }

    }

    @Throws(Exception::class)
    fun createHtmlMessage(funds: List<FundInfo>): String {

        val args = HashMap<String, Any>()
        args.put("funds", funds)

        val template = IOUtils.toString(javaClass.getResourceAsStream("/templates/mail.html"), "UTF-8")

        return engine.renderString(template, args)
    }

}