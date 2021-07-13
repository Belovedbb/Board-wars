package com.board.wars.repository.custom;

import com.board.wars.domain.Column;
import com.board.wars.repository.base.BaseMultiInterface;
import com.board.wars.repository.base.BaseSingleInterface;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

public interface CustomColumnRepository {
    
    Mono<BaseMultiInterface.BaseColumn<Column>> insertNewColumnToProject(Long code, Column column);

    Mono<BaseSingleInterface.BaseColumn<Column>> updateProjectColumn(Long code, Column column);

    Mono<BaseMultiInterface.BaseColumn<Column>> deleteProjectColumn(Long code, String name);

    Mono<BaseSingleInterface.BaseColumn<Column>> findByProjectCodeAndColumnName(Long code, String name, Pageable pageable);

    Mono<BaseMultiInterface.BaseColumn<Column>> findByProjectCode(Long code, Pageable pageable);

    Mono<BaseMultiInterface.BaseColumn<Column>> findColumnsByProjectCode(Long code, String[] name, Pageable pageable);

    Mono<BaseMultiInterface.BaseColumn<Column>> updateProjectColumns(Long projectCode, Column[] columns);

}
