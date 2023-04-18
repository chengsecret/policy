package site.koisecret.esoperation.service;

import com.opencsv.CSVReader;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.koisecret.esoperation.DTO.KeywordDTO;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author by chengsecret
 * @date 2023/4/1.
 */
@Service
public class AddKeywordsByID {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    public void addKeywords() throws IOException {
        int i = 1;
        List<KeywordDTO> list = readCsvFile();
        for (KeywordDTO keywordDTO : list) {
            UpdateRequest updateRequest = new UpdateRequest("policy", keywordDTO.getPolicyId());
            Map<String, Object> updateObject = new HashMap<>();
            updateObject.put("keywords", keywordDTO.getKeywords());

            // 执行IndexRequest请求，获取IndexResponse响应对象
            try {
                updateRequest.doc(updateObject);
                restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
            } catch (Exception e) {
                if (!e.getMessage().contains("200 OK")) {
                    throw new RuntimeException(e);
                }else {
                    System.out.println(i++);
                }
            }
        }
    }


    private List<KeywordDTO> readCsvFile() throws IOException {
        String csvFilePath = "keyword.csv";
        // 创建CSVReader对象并指定CSV文件路径
        CSVReader reader = new CSVReader(new FileReader(csvFilePath));
        List<KeywordDTO> list = new LinkedList<>();
        // 读取文件中的所有行
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            // 读取每行中的数据
            String policyId = nextLine[1];
            String[] data = nextLine[2].replaceAll("[\\[\\]\"]", "").split(", ");
            for (int i=0; i<data.length; i++) {
                String replace = data[i].replaceAll("\'", "");
                data[i] = replace;
            }
            KeywordDTO keywordDTO = new KeywordDTO();
            keywordDTO.setKeywords(data);
            keywordDTO.setPolicyId(policyId);
            list.add(keywordDTO);
        }

        // 关闭CSVReader
        reader.close();
        return list;
    }

}
