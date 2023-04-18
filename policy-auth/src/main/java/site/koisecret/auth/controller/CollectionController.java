package site.koisecret.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import site.koisecret.auth.dto.CollectionDTO;
import site.koisecret.auth.entity.Collection;
import site.koisecret.auth.entity.Policy;
import site.koisecret.auth.feign.PolicySearchFeignClient;
import site.koisecret.auth.service.CollectionService;
import site.koisecret.commons.Response.Page;
import site.koisecret.commons.Response.Result;

import java.util.List;

/**
 * @author by chengsecret
 * @date 2023/4/3.
 */
@RestController
public class CollectionController {

    @Autowired
    private CollectionService collectionService;
    @Autowired
    private PolicySearchFeignClient policySearchFeignClient;

    @PostMapping("collection/add")
    Result addCollection(@RequestHeader("uid") String uid, @RequestBody CollectionDTO collectionDTO) {
        return collectionService.addCollection(new Collection(-1,Integer.parseInt(uid),collectionDTO.getPid()));
    }

    @PostMapping("collection/del")
    Result delCollection(@RequestHeader("uid") String uid, @RequestBody CollectionDTO collectionDTO) {
        return collectionService.delCollection(new Collection(-1,Integer.parseInt(uid),collectionDTO.getPid()));
    }

    @GetMapping("collection/get")
    Result getCollections(@RequestHeader("uid") String uid, Page<Policy> page) {
        List<String> pids = collectionService.findPidsByUid(Integer.parseInt(uid));
        return policySearchFeignClient.getPoliciesByids(pids, page.getCurrent(), page.getSize(), uid);

    }

}
