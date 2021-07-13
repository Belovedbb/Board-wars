package com.board.wars.repository.custom;

import com.board.wars.domain.Column;
import com.board.wars.domain.Project;
import com.board.wars.repository.base.BaseMultiInterface;
import com.board.wars.repository.base.BaseSingleInterface;
import com.mongodb.BasicDBObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Repository
public class CustomColumnRepositoryImpl implements  CustomColumnRepository{

    final private ReactiveMongoTemplate template;

    public CustomColumnRepositoryImpl(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    public Mono<BaseMultiInterface.BaseColumn<Column>> insertNewColumnToProject(Long projectCode, Column column) {
        Query query = new Query(where("code").is(projectCode));//query.with()
        Update update = new Update().addToSet("columns", column);
        return template.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), Project.class)
                .map(project -> project::getColumns);
    }

    @Override
    public Mono<BaseSingleInterface.BaseColumn<Column>> updateProjectColumn(Long projectCode, Column column) {
        Query updateQuery = new Query(where("code").is(projectCode).and("columns.name").is(column.getName()));
        Update update = new Update().set("columns.$", column);
        return template.updateMulti(updateQuery, update, Project.class)
                .filter(result -> result.getModifiedCount() > 0)
                .flatMap(result -> findByProjectCodeAndColumnName(projectCode, column.getName(), Pageable.unpaged()));
    }

    @Override
    public Mono<BaseMultiInterface.BaseColumn<Column>> deleteProjectColumn(Long projectCode, String name) {
        Query query = new Query(Criteria.where("code").is(projectCode));
        Update update = new Update().pull("columns", new BasicDBObject("name", name));
        return template.findAndModify(query, update, Project.class).map(project -> project::getColumns);
    }

    @Override
    public Mono<BaseSingleInterface.BaseColumn<Column>> findByProjectCodeAndColumnName(Long code, String name, Pageable pageable) {
        Query query = new Query(Criteria.where("code").is(code).and("columns.name").is(name)).with(pageable);
        query.fields().include("columns.$");
        return template.findOne(query, Project.class).map(project -> () -> project.getColumns().iterator().next());
    }

    @Override
    public Mono<BaseMultiInterface.BaseColumn<Column>> findByProjectCode(Long code, Pageable pageable) {
        Query query = new Query(Criteria.where("code").is(code)).with(pageable);
        query.fields().include("columns");
        return template.findOne(query, Project.class).map(project -> project::getColumns);
    }

    @Override
    public Mono<BaseMultiInterface.BaseColumn<Column>> findColumnsByProjectCode(Long code, String[] name, Pageable pageable) {
        Query query = new Query(Criteria.where("code").is(code).and("columns.name").in(name)).with(pageable);
        query.fields().include("columns.$");
        return template.findOne(query, Project.class).map(project -> project::getColumns);
    }

    @Override
    public Mono<BaseMultiInterface.BaseColumn<Column>> updateProjectColumns(Long projectCode, Column[] columns) {
        Query updateQuery = new Query(where("code").is(projectCode));
        Update update = new Update().set("columns", columns);
        return template.updateMulti(updateQuery, update, Project.class)
                .filter(result -> result.getModifiedCount() > 0)
                .flatMap(result -> findByProjectCode(projectCode, Pageable.unpaged()));
    }

}
