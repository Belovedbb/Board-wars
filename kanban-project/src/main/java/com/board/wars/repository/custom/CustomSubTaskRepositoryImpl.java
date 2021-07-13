package com.board.wars.repository.custom;

import com.board.wars.domain.Project;
import com.board.wars.domain.SubTask;
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
public class CustomSubTaskRepositoryImpl implements CustomSubTaskRepository{

    final private ReactiveMongoTemplate template;

    public CustomSubTaskRepositoryImpl(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    public Mono<BaseMultiInterface.BaseSubTask<SubTask>> insertNewSubTask(Long code, String columnName, String taskName, SubTask subTask) {
        Query query = new Query(where("code").is(code).and("columns.name").is(columnName).and("columns.tasks.name").is(taskName));
        Update update = new Update().addToSet("columns.$.tasks.$[taskIndex].subTasks", subTask).filterArray(Criteria.where("taskIndex.name").is(taskName));
        return template.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), Project.class).map(project -> () -> getSubTasksFromProjectColumnTask(project, columnName, taskName));
    }

    @Override
    public Mono<BaseSingleInterface.BaseSubTask<SubTask>> updateSubTask(Long code, String columnName, String taskName, SubTask subTask) {
        Query updateQuery = new Query(where("code").is(code)
                .and("columns.name").is(columnName)
                .and("columns.tasks.name").is(taskName)
                .and("columns.tasks.subTasks.code").is(subTask.getCode()));
        Update update = new Update().set("columns.$[columnIndex].tasks.$[taskIndex].subTasks.$[subTaskIndex]", subTask)
                .filterArray(Criteria.where("columnIndex.name").is(columnName))
                .filterArray(Criteria.where("taskIndex.name").is(taskName))
                .filterArray(Criteria.where("subTaskIndex.code").is(subTask.getCode()));
        return template.updateMulti(updateQuery, update, Project.class)
                .filter(result -> result.getModifiedCount() > 0)
                .flatMap(result -> findByProjectColumnTask(code, columnName, taskName, subTask.getCode(), Pageable.unpaged()));
    }

    @Override
    public Mono<BaseMultiInterface.BaseSubTask<SubTask>> deleteSubTask(Long code, String columnName, String taskName, Long subTaskId) {
        Query query = new Query(where("code").is(code)
                .and("columns.name").is(columnName)
                .and("columns.tasks.name").is(taskName));
        Update update = new Update().pull("columns.$[columnIndex].tasks.$[taskIndex].subTasks", new BasicDBObject("code", subTaskId))
                .filterArray(Criteria.where("columnIndex.name").is(columnName))
                .filterArray(Criteria.where("taskIndex.name").is(taskName));
        return template.findAndModify(query, update, Project.class).map(project -> () -> getSubTasksFromProjectColumnTask(project, columnName, taskName));
    }

    @Override
    public Mono<BaseSingleInterface.BaseSubTask<SubTask>> findByProjectColumnTask(Long code, String columnName, String taskName, Long subTaskId, Pageable pageable) {
        Query query = new Query(where("code").is(code)
                .and("columns.name").is(columnName)
                .and("columns.tasks.name").is(taskName)
                .and("columns.tasks.subTasks.code").is(subTaskId)).with(pageable);
        query.fields().include("columns.tasks.subTasks.$");
        return template.findOne(query, Project.class)
                .map(project -> () -> getSubTasksFromProjectColumnTask(project, columnName, taskName).stream()
                        .filter(subTask -> subTask.getCode().equals(subTaskId)).findFirst().orElseThrow());
    }

    @Override
    public Mono<BaseMultiInterface.BaseSubTask<SubTask>> findByProject(Long code, String columnName, String taskName, Pageable pageable) {
        Query query = new Query(where("code").is(code)
                .and("columns.name").is(columnName)
                .and("columns.tasks.name").is(taskName)).with(pageable);
        query.fields().include("columns.tasks.$");
        return template.findOne(query, Project.class).map(project -> () -> getSubTasksFromProjectColumnTask(project, columnName, taskName));
    }

    private List<SubTask> getSubTasksFromProjectColumnTask(Project project, String columnName, String taskName) {
        return project.getColumns()
                .stream()
                .filter(column -> column.getName().equals(columnName))
                .findFirst().orElseThrow()
                .getTasks().stream()
                .filter(task -> taskName.equals(task.getName()))
                .findFirst().orElseThrow().getSubTasks();
    }
}
