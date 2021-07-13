package com.board.wars.mapper;

import com.board.wars.domain.FileAttachment;
import com.board.wars.payload.request.FileAttachmentRequestPayload;
import com.board.wars.payload.response.FileAttachmentResponsePayload;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper
public interface FileAttachmentMapper {

    FileAttachment mapRequestPayloadToFileAttachmentDomain(FileAttachmentRequestPayload payload);

    FileAttachmentResponsePayload mapFileAttachmentDomainToResponsePayload(FileAttachment fileAttachment);

    @BeanMapping(nullValuePropertyMappingStrategy =  NullValuePropertyMappingStrategy.IGNORE)
    FileAttachment updateFileAttachmentFromRequestPayload(@MappingTarget FileAttachment savedFileAttachment, FileAttachmentRequestPayload payload);
}
