package se.acrend.ppm.mail;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.acrend.ppm.domain.FundInfo;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 *
 */
public class FundMailerTest {

    private FundMailer mailer;

    @Before
    public void setUp() throws Exception {
        mailer = new FundMailer();
    }

    @Test
    @Ignore
    public void createHtmlMessage() throws Exception {

        List<FundInfo> funds = new ArrayList<>();

//        FundInfo fund = new FundInfo();
//        fund.setName("Fond 1");
//        fund.setUrl("url-1");
//        fund.setPpmNumber("123456");
//        funds.add(fund);
//
//        String message = mailer.createHtmlMessage(funds);
//
//        System.out.println(message);

    }

}