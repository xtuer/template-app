import edu.bean.exam.Paper;
import edu.bean.exam.Question;
import edu.bean.exam.QuestionOption;
import edu.mapper.ExamMapper;
import edu.mapper.ExamQuestionMapper;
import edu.service.ExamQuestionService;
import edu.service.ExamService;
import edu.service.IdWorker;
import edu.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * 测试 Exam
 */
@RunWith(SpringRunner.class)
@ContextConfiguration({"classpath:application-test.xml"})
public class ExamTest {
    @Autowired
    private IdWorker idWorker;

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private ExamQuestionMapper questionMapper;

    @Autowired
    private ExamService examService;

    @Autowired
    private ExamQuestionService questionService;

    /**
     * 创建试卷:
     * 1. 创建题目和选项
     * 2. 创建试卷
     * 3. 创建试卷的题目
     */
    @Test
    public void setupPaper() {
        // 创建题目
        insertQuestion();
        insertMaterialQuestion();
        insertDescriptionQuestion();

        // 创建试卷
        createPaper();
    }

    @Test
    public void insertDescriptionQuestion() {
        questionService.createOrUpdateQuestion(createDescriptionQuestion());
    }

    /**
     * 1. 创建题目
     */
    @Test
    public void insertQuestion() {
        Question question = createQuestion();
        questionService.createOrUpdateQuestion(question);
    }

    /**
     * 2. 创建材料题
     */
    @Test
    public void insertMaterialQuestion() {
        Question question = createMaterialQuestion();
        questionService.createOrUpdateQuestion(question);
    }

    /**
     * 3. 创建试卷
     */
    @Test
    public void createPaper() {
        Paper paper = new Paper();
        paper.setId(1L);
        paper.setTitle("考试我们是认真的");

        Question q1 = createDescriptionQuestion();
        q1.setPosition(0);
        q1.setGroupSn(1);

        Question q2 = createQuestion();
        q2.setPosition(1);
        q2.setGroupSn(1);
        q2.setScore(20);

        Question q3 = createMaterialQuestion();
        q3.setPosition(2);
        q3.setGroupSn(1);

        // 删除题目: 不会存在试卷的题目里，为了掩饰删除的操作
        Question q4 = createQuestion();
        q4.setId(123L);
        q4.setDeleted(true);

        paper.addQuestion(q1);
        paper.addQuestion(q2);
        paper.addQuestion(q3);
        paper.addQuestion(q4);

        examService.createOrUpdatePaper(paper);
    }

    /**
     * 测试删除题目
     */
    @Test
    public void deleteQuestion() {
        Question question = createQuestion();
        question.setDeleted(true);
        questionService.createOrUpdateQuestion(question);
    }

    /**
     * 测试删除材料题
     */
    @Test
    public void deleteMaterialQuestion() {
        Question question = createMaterialQuestion();
        question.setDeleted(true);
        questionService.createOrUpdateQuestion(question);
    }

    /**
     * 如果题目、选项没有 ID 则生成 ID
     */
    @Test
    public void ensureQuestionAndOptionIds() {
        Question question = createQuestion();
        Utils.dump(question);

        question.setId(0L);
        question.getOptions().get(0).setId(0L);
        question.getOptions().get(1).setId(null);
        questionService.ensureQuestionAndOptionIds(question);
        Utils.dump(question);
    }

    /**
     * 如果材料题的题目、小题、选项没有 ID 则生成 ID
     */
    @Test
    public void ensureMaterialQuestionAndOptionIds() {
        Question question = createMaterialQuestion();
        Utils.dump(question);

        question.setId(0L);
        question.getSubQuestions().get(1).setId(0L);
        questionService.ensureQuestionAndOptionIds(question);
        Utils.dump(question);
    }

    /**
     * 查找试卷的题目
     */
    @Test
    public void findPaperQuestions() {
        Utils.dump(examService.findPaperQuestions(1L));
    }

    /**
     * 创建一问题
     */
    private Question createQuestion() {
        Question question = new Question(10, "你是谁?", "咨询武林百晓生", Question.BASE_TYPE_SINGLE_CHOICE);
        question.addOption(new QuestionOption(101, "曹操", true,  1, 0));
        question.addOption(new QuestionOption(102, "刘备", false, 2, 0));
        question.addOption(new QuestionOption(103, "孙权", false, 3, 0));
        question.addOption(new QuestionOption(104, "卧龙", false, 4, 0));

        return question;
    }

    /**
     * 创建材料题
     */
    private Question createMaterialQuestion() {
        // 材料题只有题干，没有选项，但有小题
        Question question = new Question(20, "阅读短文回答题目", "咨询武林百晓生", Question.BASE_TYPE_COMPREHENSION);

        // 材料题的小题 1
        Question subQuestion1 = new Question(21, "挖掘机哪家强", "", Question.BASE_TYPE_SINGLE_CHOICE); // 小题 1
        subQuestion1.addOption(new QuestionOption(211, "北京大学", false, 1, 0));
        subQuestion1.addOption(new QuestionOption(212, "清华大学", false, 2, 0));
        subQuestion1.addOption(new QuestionOption(213, "山东蓝翔", true,  3, 0));
        subQuestion1.setScore(15);

        // 材料题的小题 2
        Question subQuestion2 = new Question(22, "三国谁最狠", "", Question.BASE_TYPE_SINGLE_CHOICE); // 小题 1
        subQuestion2.addOption(new QuestionOption(221, "刘备", true,  1, 0));
        subQuestion2.addOption(new QuestionOption(222, "曹操", false, 1, 0));
        subQuestion2.addOption(new QuestionOption(223, "关羽", false, 1, 0));
        subQuestion2.addOption(new QuestionOption(224, "周瑜", false, 1, 0));
        subQuestion2.setScore(20);

        question.addSubQuestion(subQuestion1);
        question.addSubQuestion(subQuestion2);

        return question;
    }

    private Question createDescriptionQuestion() {
        return new Question(30, "一、请完成下面的选择题，每题 3 分", "", Question.BASE_TYPE_DESCRIPTION);
    }

    /**
     * 查找章节的题目
     */
    @Test
    public void findQuestionsByChapterCode() {
        List<Question> questions = questionService.findQuestionsByChapterCode("gz_yw_rjb_bx01", "02-03", new Question(), 0, 100);
        Utils.dump(questions);
    }

    /**
     * 查找知识点的题目
     */
    @Test
    public void findQuestionsByKnowledgePointCode() {
        List<Question> questions = questionService.findQuestionsByKnowledgePointCode("GZ-YW", "03-01-01", new Question(), 0, 100);
        Utils.dump(questions);
    }

    /**
     * 使用 ID 查找题目: 材料题时包含小题
     */
    @Test
    public void findQuestionById() {
        Utils.dump(questionService.findQuestionById(226265858790391808L));
    }
}
