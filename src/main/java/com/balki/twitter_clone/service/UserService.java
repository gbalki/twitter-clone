package com.balki.twitter_clone.service;

import com.balki.twitter_clone.dto.UserDTO;
import com.balki.twitter_clone.exception.UserNotFoundException;
import com.balki.twitter_clone.model.User;
import com.balki.twitter_clone.repository.UserRepository;
import com.balki.twitter_clone.request.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final ModelMapper mapper;

    private final FileService fileService;

    private final JwtService jwtService;

    public Page<UserDTO> getAll(Pageable page, User currentUser) {
        if (currentUser != null) {
            Page<User> currentUserNot = userRepository.findByEmailNotAndActiveTrue(currentUser.getEmail(), page);
            return new PageImpl<>(currentUserNot.stream().map(u -> mapper.map(u, UserDTO.class)).collect(Collectors.toList()));
        }
        Page<User> users = userRepository.findUserByActiveTrue(page);
        return new PageImpl<>(users.stream().map(user -> mapper.map(user, UserDTO.class)).collect(Collectors.toList()));
    }

    public UserDTO updateUser(Long id, UserUpdateRequest userUpdateRequest) {
        User user = userRepository.findUserById(id);
        user.setFirstName(userUpdateRequest.getFirstName());
        user.setLastName(userUpdateRequest.getLastName());
        user.setDisplayName(userUpdateRequest.getDisplayName());
        if (userUpdateRequest.getImage() != null) {
            try {
                String storageFileName = fileService.writeBase64EncodedStringToFile(userUpdateRequest.getImage());
                fileService.deleteProfileImage(user.getImage());
                user.setImage(storageFileName);
            } catch (IOException e) {
                throw new RuntimeException("Incorrect file type");
            }
        }
        return mapper.map(userRepository.save(user), UserDTO.class);
    }

    public UserDTO getUserById(Long id) {
        return mapper.map(userRepository.findUserById(id), UserDTO.class);
    }

    public void deleteUser(Long id) {
        try {
            User user = userRepository.findUserById(id);
            fileService.deleteAllStoredFilesForUser(user);
            userRepository.delete(user);
        } catch (RuntimeException e) {
            throw new UserNotFoundException("User not found in this id:" + id);
        }
    }
}
