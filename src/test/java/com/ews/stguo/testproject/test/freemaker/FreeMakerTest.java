package com.ews.stguo.testproject.test.freemaker;

import com.ews.stguo.testproject.utils.file.RWFileUtils;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.Version;
import org.junit.Assert;
import org.junit.Test;

import java.io.Writer;
import java.util.Map;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class FreeMakerTest {

    @Test
    public void test01() throws Exception {
        // Step 1: Create freeMaker configuration instance.
        Configuration configuration = new Configuration(new Version("2.3.23"));
        configuration.setDefaultEncoding("UTF-8");
        // Step 2: Get template path.
        configuration.setDirectoryForTemplateLoading(RWFileUtils.getFile(""));
        // Step 3: Create data model
        Map<String, Object> dataMap = GuestReviewDataGenerator.getGuestReviewDataMap();
        // Step 4: Load template file.
        Template template = configuration.getTemplate("CommonGuestReviewJsonl.ftlx");
        Assert.assertNotNull(template);
        // Step 5: Generate data.
        Writer writer = RWFileUtils.getWriter("guest.jsonl");
        // Step 6: Output file.
        template.process(dataMap, writer);
        writer.close();
        System.out.println("Successfully!!!");
    }

    @Test
    public void test02() throws Exception {
        // Step 1: Create freeMaker configuration instance.
        Configuration configuration = new Configuration(new Version("2.3.23"));
        configuration.setDefaultEncoding("UTF-8");
        // Step 2: Get template path.
        configuration.setDirectoryForTemplateLoading(RWFileUtils.getFile(""));
        // Step 3: Create data model
        Map<String, Object> dataMap = VacationRentalDataGenerator.getVacationRentalDataMap();
        // Step 4: Load template file.
        Template template = configuration.getTemplate("CommonVacationRentalJsonl.ftlx");
        Assert.assertNotNull(template);
        // Step 5: Generate data.
        Writer writer = RWFileUtils.getWriter("vacation-rental.jsonl");
        // Step 6: Output file.
        template.process(dataMap, writer);
        writer.close();
        System.out.println("Successfully!!!");
    }

}
