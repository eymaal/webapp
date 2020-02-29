package ai.elimu.web.content.storybook;

import ai.elimu.dao.StoryBookChapterDao;
import ai.elimu.dao.StoryBookDao;
import ai.elimu.dao.StoryBookParagraphDao;
import ai.elimu.model.content.Emoji;
import ai.elimu.model.content.StoryBook;
import ai.elimu.model.content.StoryBookChapter;
import ai.elimu.model.content.StoryBookParagraph;
import ai.elimu.model.content.Word;
import ai.elimu.model.enums.Language;
import ai.elimu.model.gson.content.StoryBookChapterGson;
import ai.elimu.model.gson.content.StoryBookParagraphGson;
import ai.elimu.rest.v1.JavaToGsonConverter;
import ai.elimu.util.ConfigHelper;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/content/storybook/list")
public class StoryBookCsvExportController {
    
    private final Logger logger = Logger.getLogger(getClass());
    
    @Autowired
    private StoryBookDao storyBookDao;
    
    @Autowired
    private StoryBookChapterDao storyBookChapterDao;
    
    @Autowired
    private StoryBookParagraphDao storyBookParagraphDao;
    
    @RequestMapping(value="/storybooks.csv", method = RequestMethod.GET)
    public void handleRequest(
            HttpServletResponse response,
            OutputStream outputStream
    ) throws IOException {
        logger.info("handleRequest");
        
        Language language = Language.valueOf(ConfigHelper.getProperty("content.language"));
        List<ai.elimu.model.content.StoryBook> storyBooks = storyBookDao.readAllOrdered(language);
        logger.info("storyBooks.size(): " + storyBooks.size());
        
        CSVFormat csvFormat = CSVFormat.DEFAULT
                .withHeader(
                        "id",
                        "title",
                        "description",
                        "content_license",
                        "attribution_url",
                        "grade_level",
                        "cover_image_id",
                        "chapters"
                );
        StringWriter stringWriter = new StringWriter();
        CSVPrinter csvPrinter = new CSVPrinter(stringWriter, csvFormat);
        
        for (StoryBook storyBook : storyBooks) {
            logger.info("storyBook.getTitle(): \"" + storyBook.getTitle() + "\"");
            
            Long coverImageId = null;
            if (storyBook.getCoverImage() != null) {
                coverImageId = storyBook.getCoverImage().getId();
            }
            
            // Store chapters as JSON objects
            JSONArray chaptersJsonArray = new JSONArray();
            List<StoryBookChapter> storyBookChapters = storyBookChapterDao.readAll(storyBook);
            logger.info("storyBookChapters.size(): " + storyBookChapters.size());
            for (StoryBookChapter storyBookChapter : storyBookChapters) {
                StoryBookChapterGson storyBookChapterGson = JavaToGsonConverter.getStoryBookChapter(storyBookChapter);
                storyBookChapterGson.setStoryBook(null);
                
                List<StoryBookParagraphGson> storyBookParagraphs = new ArrayList<>();
                for (StoryBookParagraph storyBookParagraph : storyBookParagraphDao.readAll(storyBookChapter)) {
                    StoryBookParagraphGson storyBookParagraphGson = JavaToGsonConverter.getStoryBookParagraph(storyBookParagraph);
                    storyBookParagraphGson.setStoryBookChapter(null);
                    storyBookParagraphGson.setWords(null);
                    storyBookParagraphs.add(storyBookParagraphGson);
                }
                storyBookChapterGson.setStoryBookParagraphs(storyBookParagraphs);
                
                String json = new Gson().toJson(storyBookChapterGson);
                JSONObject jsonObject = new JSONObject(json);
                chaptersJsonArray.put(jsonObject);
            }
            logger.info("chaptersJsonArray: " + chaptersJsonArray);
            
            csvPrinter.printRecord(
                    storyBook.getId(),
                    storyBook.getTitle(),
                    storyBook.getDescription(),
                    storyBook.getContentLicense(),
                    storyBook.getAttributionUrl(),
                    storyBook.getGradeLevel(),
                    coverImageId,
                    chaptersJsonArray
            );
            
            csvPrinter.flush();
        }
        
        String csvFileContent = stringWriter.toString();
        
        response.setContentType("text/csv");
        byte[] bytes = csvFileContent.getBytes();
        response.setContentLength(bytes.length);
        try {
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
        } catch (IOException ex) {
            logger.error(null, ex);
        }
    }
}
