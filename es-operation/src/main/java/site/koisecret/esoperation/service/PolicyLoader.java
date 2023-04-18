package site.koisecret.esoperation.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.koisecret.esoperation.dao.PolicyDao;
import site.koisecret.esoperation.entity.Policy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class PolicyLoader {

    private static final int BATCH_SIZE = 500; // 批量操作的文档数量
    private static final int THREAD_POOL_SIZE = 3; // 线程池大小

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private PolicyDao policyRepository;

    public void load() throws IOException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        List<String[]> lines = readTsvFile();
        int size = lines.size();
        int batchCount = size / BATCH_SIZE + 1;
        for (int i = 0; i < batchCount; i++) {
            int startIndex = i * BATCH_SIZE;
            int endIndex = Math.min(startIndex + BATCH_SIZE, size);
            List<String[]> batchLines = lines.subList(startIndex, endIndex);
            executorService.execute(() -> {
                List<Policy> policies = new Vector<>();
                for (String[] line : batchLines) {
                    if (line.length < 14) {
                        System.out.println(Arrays.toString(line));
                        Policy policy = new Policy();
                        policy.setPolicyId(line[0]);
                        policy.setPolicyTitle(line[1]);
                        policy.setPolicyGrade(line[2]);
                        policy.setPubAgencyId(line[3]);
                        policy.setPubAgency(line[4]);
                        policy.setPubAgencyFullName(line[5]);
                        policy.setPubNumber(line[6]);
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
                        Date date = null;
                        try {
                            date = formatter.parse(line[7]);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                        policy.setPubTime(date);
                        policy.setPolicyType(line[8]);
                        policy.setPolicyBody(line[9]);
                        policy.setProvince("");
                        policy.setCity("");
                        policy.setPolicySource("");
                        policy.setUpdateDate("");
                        policy.setKeywords(new String[]{""});
                        policy.setCategory(new String[]{});
                        policies.add(policy);
                    } else {
                        Policy policy = new Policy();
                        policy.setPolicyId(line[0]);
                        policy.setPolicyTitle(line[1]);
                        policy.setPolicyGrade(line[2]);
                        policy.setPubAgencyId(line[3]);
                        policy.setPubAgency(line[4]);
                        policy.setPubAgencyFullName(line[5]);
                        policy.setPubNumber(line[6]);
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
                        Date date = null;
                        try {
                            date = formatter.parse(line[7]);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                        policy.setPubTime(date);
                        policy.setPolicyType(line[8]);
                        policy.setPolicyBody(line[9]);
                        policy.setProvince(line[10]);
                        policy.setCity(line[11]);
                        policy.setPolicySource(line[12]);
                        policy.setUpdateDate(line[13]);
                        policy.setKeywords(new String[]{""});
                        policy.setCategory(new String[]{});
                        policies.add(policy);
                    }
                }
                try {
                    policyRepository.saveAll(policies);
                } catch (Exception e) {
                    if (!e.getMessage().contains("200 OK")) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        System.out.println(lines.size());
    }

    private List<String[]> readTsvFile() throws IOException {
        List<String[]> lines = new ArrayList<>();
        File file = new File("policyinfo.tsv");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "GBK"));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] fields = line.split("\t");
            lines.add(fields);
        }
        reader.close();
        return lines;
    }

    //用于将新body写入csv文件，给anns处理
    public ArrayList<String[]> newBody() throws IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        List<String[]> lines = readTsvFile();
        ArrayList<String[]> list = new ArrayList<>();
        list.add(new String[]{"pid","body"});
        for (String[] line : lines) {
            String policyBody = line[9];
            String pid = line[0];

            if (policyBody.contains("font-size")) {
                String newBody = policyBody.replaceAll("\\{[^}]*\\}","").
                        replaceAll(".TRS_Editor P.TRS_Editor DIV.TRS_Editor TD.TRS_Editor TH.TRS_Editor SPAN.TRS_Editor" +
                                " FONT.TRS_Editor UL.TRS_Editor LI.TRS_Editor A","")
                        .replaceAll("var str='';","")
                        .replaceAll("\"","")
                        .replaceAll(" + "," ")
                        .replaceAll("if\\(str!=\\) else document.write\\(来源：科技部\\); 【字号： 大 中 小】 --> #share-box #share-box a.bshare-weixin","")
                        .replaceAll(".TRS_Editor P .TRS_Editor DIV .TRS_Editor TD .TRS_Editor TH .TRS_Editor SPAN .TRS_Editor FONT .TRS_Editor UL .TRS_Editor LI .TRS_Editor A","")
                        .replaceAll("Normal 0 7.8 磅 0 2 false false false MicrosoftInternetExplorer4 /\\* Style Definitions \\*/ table.MsoNormalTable","")
                        .replaceAll("var str=","");
                list.add(new String[]{pid, newBody});
            }

            if (policyBody.contains("FONT-SIZE")) {
                String newBody = policyBody.replaceAll("\\{[^}]*\\}","")
                        .replaceAll(" + "," ")
                        .replaceAll(".h1 .h2 .h3[^}]*union TD","")
                        .replaceAll("P.p0 SPAN.10 SPAN.15 P.p16 .TRS_PreAppend DIV.Section0","")
                        .replaceAll(".TRS_Editor P .TRS_Editor DIV .TRS_Editor TD .TRS_Editor TH .TRS_Editor SPAN .TRS_Editor FONT .TRS_Editor UL .TRS_Editor LI .TRS_Editor A .TRS_Editor P.TRS_Editor DIV.TRS_Editor TD.TRS_Editor TH.TRS_Editor SPAN.TRS_Editor FONT.TRS_Editor UL.TRS_Editor LI.TRS_Editor A","")
                        .replaceAll(".TRS_Editor LI .TRS_Editor A","")
                        .replaceAll("MsoNormal SPAN.10 SPAN.15 P.MsoFooter P.MsoHeader SPAN.msoIns SPAN.msoDel","")
                        .replaceAll(".TRS_Editor P .TRS_Editor DIV .TRS_Editor TD .TRS_Editor TH .TRS_Editor SPAN .TRS_Editor FONT .TRS_Editor UL","")
                        .replaceAll("rmal SPAN.10 SPAN.15 P.MsoFooter P.p SPAN.msoIns SPAN.msoDel .TRS_Editor P.TRS_Editor DIV.TRS_Editor TD.TRS_Editor TH.TRS_Editor SPAN.TRS_Editor FONT.TRS_Editor UL.TRS_Editor LI.TRS_Editor","")
                        .replaceAll("P.MsoNormal[^}]*DIV.Section0","")
                        .replaceAll("P.MsoNormal[^}]*SPAN.msoDel","")
                        .replaceAll("P.p0[^}]*DIV.Section0","")
                        .replaceAll("P.MsoNormal[^}]*DIV.Section1","")
                        .replaceAll("P.MsoNormal[^}]*TRS_PreAppend UL","")
                        .replaceAll("P.MsoNormal[^}]*DIV.WordSection1","")
                        .replaceAll("screen.width-333\\)this.width=screen.width-333\"\" align=center border=0 />","")
                        .replaceAll("P.p0","").replaceAll(".h1 .h2 .h3 .union .union TD","")
                        .replaceAll("DIV.Section1","")
                        .replaceAll(".TRS_PreAppend","").replaceAll("DIV.Section0","")
                        .replaceAll(".TRS_Editor P.TRS_Editor DIV.TRS_Editor TD.TRS_Editor TH.TRS_Editor SPAN.TRS_Editor FONT.TRS_Editor UL.TRS_Editor LI.TRS_Editor A","")
                        .replaceAll("P.MsoNo A","").replaceAll("TABLE.MsoNormalTable","").replaceAll("OL  UL","")
                        .replaceAll("TABLE.MsoNormalTable","")
                        .replaceAll("SPAN.10 SPAN.15 P.p16","")
                        .replaceAll("DIV.Section2","").replaceAll(" DIV.Section3","").replaceAll("DIV.Section4","")
                        .replaceAll("DIV.Section5","")
                        .replaceAll("P.","").replaceAll("P.p15","")
                        .replaceAll(" + "," ");

                list.add(new String[]{pid, newBody});
            }

            if (policyBody.contains("Editor") || policyBody.contains("<p")) {
                String newBody = policyBody.replaceAll("\\{[^}]*\\}", "")
                        .replaceAll(" + ", " ")
                        .replaceAll(".h1 .h2 .h3[^}]*union TD", "")
                        .replaceAll(".TRS_Editor P .TRS_Editor DIV .TRS_Editor TD .TRS_Editor TH .TRS_Ed" +
                                "itor SPAN .TRS_Editor FONT .TRS_Editor UL .TRS_Editor LI .TRS_Editor A P.MsoNormal " +
                                "SPAN.msoIns SPAN.msoDel .TRS_PreAppend DIV.Section0", "")
                        .replaceAll(".TRS_Editor[^}]*LI.TRS_Editor A", "")
                        .replaceAll(".TRS_Editor[^}]*TRS_Editor A", "")
                        .replaceAll(".TRS_EditorP", "")
                        .replaceAll(".TRS_Editor[^}]*TRS_Editor BR", "")
                        .replaceAll("．TRS＿Editor[^}]*font－size：12pt；｝", "")
                        .replaceAll("<p style[^}]*break-word;=>", "")
                        .replaceAll("<p style[^}]*9;=", "")
                        .replaceAll("<p style[^}]*break-word;", "");
                list.add(new String[]{pid, newBody});
            }
        }
          return list;
    }


    //跟新policybody，去除html等杂乱信息
    public void updateBody() throws IOException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        List<String[]> lines = readTsvFile();
        int size = lines.size();
        int batchCount = size / BATCH_SIZE + 1;
        for (int i = 0; i < batchCount; i++) {
            int startIndex = i * BATCH_SIZE;
            int endIndex = Math.min(startIndex + BATCH_SIZE, size);
            List<String[]> batchLines = lines.subList(startIndex, endIndex);
            executorService.execute(() -> {

                for (String[] line : batchLines) {

                    String policyBody = line[9];
                    String pid = line[0];

                    if (policyBody.contains("font-size")) {
                        String newBody = policyBody.replaceAll("\\{[^}]*\\}","").
                                replaceAll(".TRS_Editor P.TRS_Editor DIV.TRS_Editor TD.TRS_Editor TH.TRS_Editor SPAN.TRS_Editor" +
                                        " FONT.TRS_Editor UL.TRS_Editor LI.TRS_Editor A","")
                                .replaceAll("var str='';","")
                                .replaceAll("\"","")
                                .replaceAll(" + "," ")
                                .replaceAll("if\\(str!=\\) else document.write\\(来源：科技部\\); 【字号： 大 中 小】 --> #share-box #share-box a.bshare-weixin","")
                                .replaceAll(".TRS_Editor P .TRS_Editor DIV .TRS_Editor TD .TRS_Editor TH .TRS_Editor SPAN" +
                                        " .TRS_Editor FONT .TRS_Editor UL .TRS_Editor LI .TRS_Editor A","")
                                .replaceAll("Normal 0 7.8 磅 0 2 false false false MicrosoftInternetExplorer4 /\\* Style Definitions \\*/ table.MsoNormalTable","")
                                .replaceAll("var str=","");

                        categoryService.updateBody(pid,newBody);
                    }

//                    if (policyBody.contains("FONT-SIZE")) {
//                        String newBody = policyBody.replaceAll("\\{[^}]*\\}","")
//                                .replaceAll(" + "," ")
//                                .replaceAll(".h1 .h2 .h3[^}]*union TD","")
//                                .replaceAll("P.p0 SPAN.10 SPAN.15 P.p16 .TRS_PreAppend DIV.Section0","")
//                                .replaceAll(".TRS_Editor P .TRS_Editor DIV .TRS_Editor TD .TRS_Editor TH .TRS_Editor SPAN .TRS_Editor FONT .TRS_Editor UL .TRS_Editor LI .TRS_Editor A .TRS_Editor P.TRS_Editor DIV.TRS_Editor TD.TRS_Editor TH.TRS_Editor SPAN.TRS_Editor FONT.TRS_Editor UL.TRS_Editor LI.TRS_Editor A","")
//                                .replaceAll(".TRS_Editor LI .TRS_Editor A","")
//                                .replaceAll("MsoNormal SPAN.10 SPAN.15 P.MsoFooter P.MsoHeader SPAN.msoIns SPAN.msoDel","")
//                                .replaceAll(".TRS_Editor P .TRS_Editor DIV .TRS_Editor TD .TRS_Editor TH .TRS_Editor SPAN .TRS_Editor FONT .TRS_Editor UL","")
//                                .replaceAll("rmal SPAN.10 SPAN.15 P.MsoFooter P.p SPAN.msoIns SPAN.msoDel .TRS_Editor P.TRS_Editor DIV.TRS_Editor TD.TRS_Editor TH.TRS_Editor SPAN.TRS_Editor FONT.TRS_Editor UL.TRS_Editor LI.TRS_Editor","")
//                                .replaceAll("P.MsoNormal[^}]*DIV.Section0","")
//                                .replaceAll("P.MsoNormal[^}]*SPAN.msoDel","")
//                                .replaceAll("P.p0[^}]*DIV.Section0","")
//                                .replaceAll("P.MsoNormal[^}]*DIV.Section1","")
//                                .replaceAll("P.MsoNormal[^}]*TRS_PreAppend UL","")
//                                .replaceAll("P.MsoNormal[^}]*DIV.WordSection1","")
//                                .replaceAll("screen.width-333\\)this.width=screen.width-333\"\" align=center border=0 />","")
//                                .replaceAll("P.p0","").replaceAll(".h1 .h2 .h3 .union .union TD","")
//                                .replaceAll("DIV.Section1","")
//                                .replaceAll(".TRS_PreAppend","").replaceAll("DIV.Section0","")
//                                .replaceAll(".TRS_Editor P.TRS_Editor DIV.TRS_Editor TD.TRS_Editor TH.TRS_Editor SPAN.TRS_Editor FONT.TRS_Editor UL.TRS_Editor LI.TRS_Editor A","")
//                                .replaceAll("P.MsoNo A","").replaceAll("TABLE.MsoNormalTable","").replaceAll("OL  UL","")
//                                .replaceAll("TABLE.MsoNormalTable","")
//                                .replaceAll("SPAN.10 SPAN.15 P.p16","")
//                                .replaceAll("DIV.Section2","").replaceAll(" DIV.Section3","").replaceAll("DIV.Section4","")
//                                .replaceAll("DIV.Section5","")
//                                .replaceAll("P.","").replaceAll("P.p15","")
//                                .replaceAll(" + "," ");
//
//                        System.out.println(pid + "====" +newBody);
//
//                        categoryService.updateBody(pid,newBody);
//                    }

                    if (policyBody.contains("Editor") || policyBody.contains("<p")) {
                        String newBody = policyBody.replaceAll("\\{[^}]*\\}","")
                                .replaceAll(" + "," ")
                                .replaceAll(".h1 .h2 .h3[^}]*union TD","")
                                .replaceAll(".TRS_Editor P .TRS_Editor DIV .TRS_Editor TD .TRS_Editor TH .TRS_Ed" +
                                        "itor SPAN .TRS_Editor FONT .TRS_Editor UL .TRS_Editor LI .TRS_Editor A P.MsoNormal " +
                                        "SPAN.msoIns SPAN.msoDel .TRS_PreAppend DIV.Section0","")
                                .replaceAll(".TRS_Editor[^}]*LI.TRS_Editor A","")
                                .replaceAll(".TRS_Editor[^}]*TRS_Editor A","")
                                .replaceAll(".TRS_EditorP","")
                                .replaceAll(".TRS_Editor[^}]*TRS_Editor BR","")
                                .replaceAll("．TRS＿Editor[^}]*font－size：12pt；｝","")
                                .replaceAll("<p style[^}]*break-word;=>","")
                                .replaceAll("<p style[^}]*9;=","")
                                .replaceAll("<p style[^}]*break-word;","");

                        System.out.println(pid + "====" +newBody);
                        categoryService.updateBody(pid,newBody);

                    }

                }

            });
        }
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }


}