package site.koisecret.esoperation;

import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.test.context.junit4.SpringRunner;
import site.koisecret.esoperation.entity.Policy;
import site.koisecret.esoperation.service.AddKeywordsByID;
import site.koisecret.esoperation.service.CategoryService;
import site.koisecret.esoperation.service.PolicyLoader;
import site.koisecret.esoperation.service.ToMysql;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author by chengsecret
 * @date 2023/4/1.
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class esOperation {

    @Autowired
    private  ElasticsearchRestTemplate elasticsearchTemplate;

    @Autowired
    private  PolicyLoader policyLoader;

    @Autowired
    private AddKeywordsByID addKeywordsByID;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ToMysql toMysql;


    /**
     * 创建索引并传入es数据库
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void add() throws IOException, InterruptedException {
        elasticsearchTemplate.indexOps(Policy.class).createMapping();
        policyLoader.load();
    }

    /**
     * 删除索引
     */
    @Test
    public void delete() {
        IndexOperations indexOperations = elasticsearchTemplate.indexOps(Policy.class);
        indexOperations.delete();
    }

    /**
     * 根据policyId传入keyword
     */
    @Test
    public void addKeyword() throws IOException, CsvValidationException {
        addKeywordsByID.addKeywords();
    }



    @Test
    public void category() throws IOException {

        HashMap<String, ArrayList<String>> map = new HashMap<>();
        classifyCategory(map);
//        categoryService.addCategory(map);
    }

    private void classifyCategory(HashMap<String, ArrayList<String>> map) throws IOException {
        String text = "经济 投资 税率 贷款 出口 证券 房地产 金融";
        categoryService.category(text,"经济",map);

        text = "社会 工资";
        categoryService.category(text,"社会",map);

        text = "科技 人才";
        categoryService.category(text,"科技",map);

        text = "教育 学校 招生";
        categoryService.category(text,"教育",map);

        text = "环境 污染 自然 退耕还林 林业 草原 沙漠 大风 雨雪 垃圾 水土";
        categoryService.category(text,"环境",map);

        text = "安全";
        categoryService.category(text,"安全",map);

        text = "工业";
        categoryService.category(text,"工业",map);

        text = "农业 乡村 脱贫 农民";
        categoryService.category(text,"农业",map);

        text = "文化";
        categoryService.category(text,"文化",map);

        text = "财政 央企 税率 货币 免税";
        categoryService.category(text,"财政",map);

        text = "商业 企业 市场";
        categoryService.category(text,"商业",map);

        text = "土地 拆迁 住房";
        categoryService.category(text,"土地",map);

        text = "医疗 卫生 药品 感染 疫情 残疾";
        categoryService.category(text,"医疗",map);

        text = "交通 公路 汽车";
        categoryService.category(text, "交通", map);

        text = "法律 法院";
        categoryService.category(text, "法律", map);

        text = "政务 行政 改革 政治";
        categoryService.category(text, "政治", map);

        text = "工程 项目";
        categoryService.category(text, "工程", map);

        System.out.println(map.size());
    }

    @Test
    public void updateCategory() {
        categoryService.updateCategory("100415552");
    }

    //去除body中的乱码等
    @Test
    public void updateBody() throws IOException, InterruptedException {
        policyLoader.updateBody();
    }

    @Test
    //将新政策写入csv，给anns使用
    public void writeBody() throws IOException {
        //表格数据
        ArrayList<String[]> lines = policyLoader.newBody();
        System.out.println(Arrays.toString(lines.get(0)));
        //设置路径及文件名称
        String fileName = "body.csv";
        //写入数据
        writeCSV(fileName, lines);
    }

    @Test
    //将新政策写入csv，给anns使用
    public void writeCategory() throws IOException {
        //表格数据
        HashMap<String, ArrayList<String>> map = new HashMap<>();
        classifyCategory(map);
        //设置路径及文件名称
        String fileName = "category.csv";
        ArrayList<String[]> lines = new ArrayList<>();
        lines.add(new String[]{"pid","category"});
        for (String id : map.keySet()) {
            lines.add(new String[]{id,map.get(id).toString()});
        }
        //写入数据
        writeCSV(fileName, lines);
    }

    private static void writeCSV(final String fileName, final List<String[]> data) {
        CSVWriter writer = null;
        try {
            // 创建文件所在目录
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            fileOutputStream.write(0xef); //加上这句话
            fileOutputStream.write(0xbb); //加上这句话
            fileOutputStream.write(0xbb); //加上这句话
            writer = new CSVWriter(new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8),
                    CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
            writer.writeAll(data);
        } catch (Exception e) {
            System.out.println("将数据写入CSV出错：" + e);
        } finally {
            if (null != writer) {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    System.out.println("关闭文件输出流出错：" + e);
                }
            }
        }
    }

    @Test
    public void writeToSql() throws IOException, InterruptedException {
        toMysql.write();
    }


}
