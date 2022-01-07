package com.jqp.admin.page.service.impl;

import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.page.data.Page;
import com.jqp.admin.page.data.PageButton;
import com.jqp.admin.page.data.PageQueryField;
import com.jqp.admin.page.data.PageResultField;
import com.jqp.admin.page.service.PageDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service("pageDao")
public class PageDaoImpl implements PageDao {
    @Resource
    private JdbcService jdbcService;

    @Override
    public Page get(Long id) {
        Page page = jdbcService.getById(Page.class, id);
        this.get(page);
        return page;
    }
    @Override
    public Page get(String pageCode) {
        Page page = jdbcService.findOne(Page.class,"code",pageCode);
        this.get(page);
        return page;
    }
    private void get(Page page){
        List<PageResultField> pageResultFields = jdbcService.find(PageResultField.class,"pageId",page.getId());
        page.setResultFields(pageResultFields);

        List<PageQueryField> pageQueryFields = jdbcService.find(PageQueryField.class,"pageId",page.getId());
        page.setQueryFields(pageQueryFields);

        List<PageButton> pageButtons = jdbcService.find(PageButton.class,"pageId",page.getId());
        page.setPageButtons(pageButtons);
    }
    @Override
    @Transactional
    public void save(Page page) {
        jdbcService.saveOrUpdate(page);

        jdbcService.update("delete from page_result_field where page_id = ? ",page.getId());
        int seq = 0;
        for (PageResultField field:
                page.getResultFields()) {
            field.setId(null);
            field.setPageId(page.getId());
            field.setSeq(++seq);
            jdbcService.saveOrUpdate(field);
        }
        jdbcService.update("delete from page_query_field where page_id = ? ",page.getId());
        seq = 0;
        for (PageQueryField field:
                page.getQueryFields()) {
            field.setId(null);
            field.setPageId(page.getId());
            field.setSeq(++seq);
            jdbcService.saveOrUpdate(field);
        }

        jdbcService.update("delete from page_button where page_id = ? ",page.getId());
        seq = 0;
        for (PageButton button:
                page.getPageButtons()) {
            button.setId(null);
            button.setPageId(page.getId());
            button.setSeq(++seq);
            jdbcService.saveOrUpdate(button);
        }
    }
}