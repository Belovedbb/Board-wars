package com.board.wars.repository;

import com.board.wars.domain.Column;
import com.board.wars.repository.base.BaseMultiInterface;
import com.board.wars.repository.base.BaseSingleInterface;
import com.board.wars.repository.custom.CustomColumnRepository;

public interface ColumnRepository extends ProjectRepository, BaseSingleInterface<Column>,
        BaseMultiInterface<Column>, CustomColumnRepository {
}
