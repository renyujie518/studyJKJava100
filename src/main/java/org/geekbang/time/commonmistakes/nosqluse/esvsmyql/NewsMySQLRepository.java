package org.geekbang.time.commonmistakes.nosqluse.esvsmyql;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsMySQLRepository extends JpaRepository<News, Long> {
    /**
     * @description 代表查询条件是：搜索分类等于 cateid 参数，且内容同时包含关键字 keyword1 和 keyword2，计算符合条件的新闻总数量：
     */
    long countByCateidAndContentContainingAndContentContaining(int cateid, String keyword1, String keyword2);
}
