package se.acrend.ppm.mail

import com.sendgrid.*
import org.apache.commons.io.IOUtils
import org.rythmengine.RythmEngine
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import se.acrend.ppm.domain.FundInfo
import se.acrend.ppm.domain.Strategy
import java.util.*


/**
 *
 */
@Component
class FundMailer(val engine: RythmEngine, val environment: Environment) {

    val logger = LoggerFactory.getLogger(FundMailer::class.java)


    fun sendMail(subject: String, contents: String): Mono<Boolean> {

        val key = environment.getProperty("sendgrid.api.key")
        logger.info("Sendgrid key $key")

        val sendGrid = SendGrid(key)

        val from = Email("mailer@ppmselector.appspotmail.com", "PPM-väljaren")
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

    fun createHtmlMessage(fund: FundInfo, strategy: Strategy): Mono<String> {

        val funds: List<FundInfo> = Arrays.asList(fund)

        val args = HashMap<String, Any>()
        args.put("funds", funds)
        args.put("strategy", strategy)

        val template = IOUtils.toString(javaClass.getResourceAsStream("/templates/mail.html"), "UTF-8")

        return Mono.just(engine.renderString(template, args))
    }

}