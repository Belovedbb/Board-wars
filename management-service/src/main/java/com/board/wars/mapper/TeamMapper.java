package com.board.wars.mapper;

import com.board.wars.domain.Team;
import com.board.wars.domain.User;
import com.board.wars.payload.request.TeamRequestPayload;
import com.board.wars.payload.request.UserRequestPayload;
import com.board.wars.payload.response.TeamResponsePayload;
import com.board.wars.payload.response.UserResponsePayload;
import org.mapstruct.*;

import java.util.Arrays;
import java.util.stream.Collectors;

@Mapper(uses = {UserMapper.class})
public interface TeamMapper {

    @Mapping(target = "members", source = "members", qualifiedByName = "membersMapperResponse")
    @Mapping(target = "leader", source = "leader", qualifiedByName = "leaderMapperResponse")
    @Mapping(target = "dateCreated", source = "timeCreated")
    TeamResponsePayload mapTeamDomainToResponsePayload(Team team, @Context UserMapper userMapper);

    @Mapping(target = "members", source = "members", qualifiedByName = "membersMapper")
    @Mapping(target = "leader", source = "leader", qualifiedByName = "leaderMapper")
    @BeanMapping(nullValuePropertyMappingStrategy =  NullValuePropertyMappingStrategy.IGNORE)
    Team mapTeamRequestPayloadToTeam(@MappingTarget Team savedTeam, TeamRequestPayload payload);

    @Mapping(target = "members", source = "members", qualifiedByName = "membersMapper")
    @Mapping(target = "leader", source = "leader", qualifiedByName = "leaderMapper")
    Team mapRequestPayloadToTeamDomain(TeamRequestPayload payload);

    @Named("membersMapper")
    default User[] membersMapper(UserRequestPayload[] userRequestPayloads) {
       if(userRequestPayloads != null && userRequestPayloads.length > 0){
           return Arrays.stream(userRequestPayloads)
                   .map(payload -> getGeneratedUser(payload.getUsername()))
                   .collect(Collectors.toList())
                   .toArray(User[]::new);
       }
       return new User[]{};
    }

    @Named("leaderMapper")
    default User leaderMapper(UserRequestPayload userRequestPayload) {
        return getGeneratedUser(userRequestPayload.getUsername());
    }

    default User getGeneratedUser(String username){
        User user = new User();
        user.setUsername(username);
        return user;
    }

    @Named("membersMapperResponse")
    default UserResponsePayload[] membersMapperRequest(User[] user, @Context UserMapper userMapper) {
        if(user != null && user.length > 0){
            return Arrays.stream(user)
                    .map(userDomain -> getGeneratedUserResponse(userDomain, userMapper))
                    .collect(Collectors.toList())
                    .toArray(UserResponsePayload[]::new);
        }
        return new UserResponsePayload[]{};
    }

    @Named("leaderMapperResponse")
    default UserResponsePayload leaderMapperRequest(User user, @Context UserMapper userMapper) {
        return getGeneratedUserResponse(user, userMapper);
    }

    default UserResponsePayload getGeneratedUserResponse(User user, UserMapper userMapper){
        return userMapper.mapUserDomainToResponsePayload(user);
    }
}
