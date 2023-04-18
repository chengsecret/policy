package site.koisecret.policy.search.mapper;

import org.springframework.stereotype.Repository;
import site.koisecret.policy.search.entity.SearchHistory;

/**
 * @author by chengsecret
 * @date 2023/4/9.
 */
@Repository
public interface SearchHistoryMapper {

    boolean addSearchHistory(SearchHistory searchHistory);
}
