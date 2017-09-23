package se.acrend.ppm.mail

import com.sendgrid.*
import org.apache.commons.io.IOUtils
import org.rythmengine.RythmEngine
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import se.acrend.ppm.domain.FundInfo
import java.util.*


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


    fun sendMail(contents: String): Mono<Boolean> {

        val key = System.getProperty("sendgrid.api.key")
        val sendGrid = SendGrid(key)

        val from = Email("mailer@ppmselector.appspotmail.com", "PPM-väljaren")
        val subject = "Nytt val för PPM-fonder!"
        val to = Email("johan.kindgren@gmail.com")
//        val to = Email("ppmselector@googlegroups.com")

        val mail = Mail(from, subject, to, Content("text/plain", "Dags att byta fonder igen!"))
        mail.addContent(Content("text/html", contents))

        val request = Request()

        request.method = Method.POST
        request.endpoint = "mail/send"
        request.body = mail.build()
        sendGrid.api(request)

        return Mono.just(true)
    }

    fun createHtmlMessage(fund: FundInfo): Mono<String> {

        val funds: List<FundInfo> = Arrays.asList(fund)

        val args = HashMap<String, Any>()
        args.put("funds", funds)

        val template = IOUtils.toString(javaClass.getResourceAsStream("/templates/mail.html"), "UTF-8")

        return Mono.just(engine.renderString(template, args))
    }

}