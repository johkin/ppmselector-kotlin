package se.acrend.ppm.parser;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.acrend.ppm.domain.FundInfo;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.*;

/**
 *
 */
public class MorningstarParserTest {

    private MorningstarParser parser;

    @Before
    public void setUp() throws Exception {
        parser = new MorningstarParser();
    }

    @Test
    @Ignore
    public void parseList() throws Exception {
        String contents = new String(Files.readAllBytes(Paths.get(getClass().getResource("/html/morningstar/fundlist.html").toURI())));

        List<FundInfo> result = parser.parseReturns(contents);

        assertEquals(20, result.size());

        FundInfo fund = result.get(0);

        assertEquals("22/6", fund.getDate());
        assertEquals("C WorldWide Medical Small & Mid Cap 1A", fund.getName());
        assertEquals("http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000YRMG&programid=0000000000", fund.getUrl());
        assertEquals("1,0", fund.getGrowthDay());
        assertEquals("10,1", fund.getGrowthWeek());
        assertEquals("16,0", fund.getGrowthMonth());
        assertEquals("12,2", fund.getGrowth3Month());
        assertEquals("25,2", fund.getGrowth6Month());
        assertEquals("29,5", fund.getGrowthYear());

    }

    @Test
    @Ignore
    public void parseDetails() throws Exception {
        String contents = new String(Files.readAllBytes(Paths.get(getClass().getResource("/html/morningstar/funddetails.html").toURI())));

//        FundInfo info = parser.parseFund(contents, new FundInfo());
//
//        assertEquals("538108", info.getPpmNumber());
//        assertEquals("1,75%", info.getFee());


    }

}