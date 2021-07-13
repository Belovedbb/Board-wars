package com.board.wars.repository.custom;

import com.board.wars.domain.Project;
import com.board.wars.domain.TaskComment;
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

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Repository
public class CustomTaskCommentRepositoryImpl implements CustomTaskCommentRepository{

    final private ReactiveMongoTemplate template;

    public CustomTaskCommentRepositoryImpl(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    public Mono<BaseMultiInterface.BaseTaskComment<TaskComment>> insertNewTaskComment(Long code, String columnName, String taskName, TaskComment taskComment) {
        Query query = new Query(where("code").is(code).and("columns.name").is(columnName).and("columns.tasks.name").is(taskName));
        Update update = new Update().addToSet("columns.$.tasks.$[taskIndex].comments", taskComment).filterArray(Criteria.where("taskIndex.name").is(taskName));
        return template.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), Project.class).map(project -> () -> getTaskCommentsFromProjectColumnTask(project, columnName, taskName));
    }

    @Override
    public Mono<BaseSingleInterface.BaseTaskComment<TaskComment>> updateTaskComment(Long code, String columnName, String taskName, TaskComment taskComment) {
        Query updateQuery = new Query(where("code").is(code)
                .and("columns.name").is(columnName)
                .and("columns.tasks.name").is(taskName)
                .and("columns.tasks.comments.code").is(taskComment.getCode()));
        Update update = new Update().set("columns.$[columnIndex].tasks.$[taskIndex].comments.$[taskCommentIndex]", taskComment)
                .filterArray(Criteria.where("columnIndex.name").is(columnName))
                .filterArray(Criteria.where("taskIndex.name").is(taskName))
                .filterArray(Criteria.where("taskCommentIndex.code").is(taskComment.getCode()));
        return template.updateMulti(updateQuery, update, Project.class)
                .filter(result -> result.getModifiedCount() > 0)
                .flatMap(result -> findByProjectColumnTask(code, columnName, taskName, taskComment.getCode(), Pageable.unpaged()));
    }

    @Override
    public Mono<BaseMultiInterface.BaseTaskComment<TaskComment>> deleteTaskComment(Long code, String columnName, String taskName, Long taskCommentId) {
        Query query = new Query(where("code").is(code)
                .and("columns.name").is(columnName)
                .and("columns.tasks.name").is(taskName));
        Update update = new Update().pull("columns.$[columnIndex].tasks.$[taskIndex].comments", new BasicDBObject("code", taskCommentId))
                .filterArray(Criteria.where("columnIndex.name").is(columnName))
                .filterArray(Criteria.where("taskIndex.name").is(taskName));
        return template.findAndModify(query, update, Project.class).map(project -> () -> getTaskCommentsFromProjectColumnTask(project, columnName, taskName));
    }

    @Override
    public Mono<BaseSingleInterface.BaseTaskComment<TaskComment>> findByProjectColumnTask(Long code, String columnName, String taskName, Long taskCommentId, Pageable pageable) {
        Query query = new Query(where("code").is(code)
                .and("columns.name").is(columnName)
                .and("columns.tasks.name").is(taskName)
                .and("columns.tasks.comments.code").is(taskCommentId)).with(pageable);
        query.fields().include("columns.tasks.comments.$");
        return template.findOne(query, Project.class)
                .map(project -> () -> getTaskCommentsFromProjectColumnTask(project, columnName, taskName).stream()
                        .filter(taskComment -> taskComment.getCode().equals(taskCommentId)).findFirst().orElseThrow());
    }

    @Override
    public Mono<BaseMultiInterface.BaseTaskComment<TaskComment>> findByProject(Long code, String columnName, String taskName, Pageable pageable) {
        Query query = new Query(where("code").is(code)
                .and("columns.name").is(columnName)
                .and("columns.tasks.name").is(taskName)).with(pageable);
        query.fields().include("columns.tasks.$");
        return template.findOne(query, Project.class).map(project -> () -> getTaskCommentsFromProjectColumnTask(project, columnName, taskName));
    }

    private List<TaskComment> getTaskCommentsFromProjectColumnTask(Project project, String columnName, String taskName) {
        return project.getColumns()
                .stream()
                .filter(column -> column.getName().equals(columnName))
                .findFirst().orElseThrow()
                .getTasks().stream()
                .filter(task -> taskName.equals(task.getName()))
                .findFirst().orElseThrow().getComments();
    }
}
