import edu.bean.qa.Question;
import edu.service.ElasticSearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 测试 elastic search
 */
@RunWith(SpringRunner.class)
@ContextConfiguration({"classpath:config/application.xml"})
public class ElasticsearchTest {

    @Autowired
    ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    ElasticSearchService elasticSearchService;

    @Test
    public void test() {
        Question question1 = new Question(120L, "极限求解有几种方式？");
        Question question2 = new Question(121L, "什么叫凸函数？");
        Question question3 = new Question(122L, "怎么求解极小值？");
        /*elasticsearchTemplate.deleteIndex(Question.class);*/
        /*elasticsearchTemplate.createIndex(Question.class);*/
        /*elasticsearchTemplate.putMapping(Question.class);*/
        /*elasticSearchService.createOrUpdateIndex(question1, question1.getId());
        elasticSearchService.createOrUpdateIndex(question2, question2.getId());
        elasticSearchService.createOrUpdateIndex(question3, question3.getId());*/
        question1.setTitle("什么叫似然函数？");
        elasticSearchService.createOrUpdateIndex(question1, question1.getId());
    }
}
