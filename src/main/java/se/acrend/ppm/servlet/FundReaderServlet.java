package se.acrend.ppm.servlet;

import se.acrend.ppm.domain.FundInfo;
import se.acrend.ppm.mail.FundMailer;
import se.acrend.ppm.parser.MorningstarParser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class FundReaderServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(FundReaderServlet.class.getName());

    private MorningstarParser parser = new MorningstarParser();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        PrintWriter writer = resp.getWriter();
        resp.setContentType("text/plain");

        URL url = new URL("http://www.morningstar.se/Funds/Quickrank.aspx?ppm=on&sort=Week_1&ascdesc=Desc&view=returns");
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
        StringBuilder contents = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            contents.append(line);
        }
        reader.close();

        writer.println("Läst listan från Morningstar");
        log.log(Level.INFO, "Fundlist read");

        List<FundInfo> funds = parser.parseReturns(contents.toString());

        for (FundInfo fund : funds) {
            updateFund(fund);
            writer.println("Hämtat detaljer för " + fund.getName());
            log.log(Level.INFO, "Hämtat detaljer för " + fund.getName());
        }

        FundMailer mailer = new FundMailer();

        try {
            mailer.sendMail(funds);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Can't send mail!", e);
            e.printStackTrace(writer);
        }
        writer.println("Skickat mail");

    }

    void updateFund(FundInfo fund) throws IOException {

        URL url = new URL(fund.getUrl());
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
        StringBuilder contents = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            contents.append(line);
        }
        reader.close();

        parser.parseFund(contents.toString(), fund);
    }
}
