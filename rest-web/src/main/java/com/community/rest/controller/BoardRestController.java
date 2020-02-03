package com.community.rest.controller;

import com.community.rest.domain.Board;
import com.community.rest.repository.BoardRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.PagedModel.PageMetadata;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

//1. controller 생성
//2. REST API 컨트롤러의 POST(생성),PUT(수정),DELETE(삭제) 메서드

@RestController
@RequestMapping("/api/boards")
public class BoardRestController {
    private BoardRepository boardRepository;

    //1.
    // 생성자 의존성 주입
    public BoardRestController(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    //1.
    // Get방식으로 '/api/boards'호출 시 해당 메서드에 매핑, 반환값은 JSON타입
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getBoards(@PageableDefault Pageable pageable) {
        Page<Board> boards = boardRepository.findAll(pageable);

        //hateoas 1.0 이후 변동사항
        //1.
        //현재 페이지 수, 총 게시판 수, 한 페이지의 게시판 수 등 페이징 처리에 관한 리소스를 만드는
        //PagedModel 객체를 생성하기 위해 PagedModel 생성자의 파라미터로 사용되는 PageMetadata 객체를 생성
        //PageMetadata는 전체 페이지 수, 현재 페이지 번호, 총 게시판 수로 구성
        PageMetadata pageMetadata = new PageMetadata(pageable.getPageSize(),
                boards.getNumber(), boards.getTotalElements());
        //1.
        //PagedModel 객체를 생성
        //이 객체를 생성하면 HATEOAS가 적용되며 페이징값까지 생성된 REST형의 데이터를 만들어준다
        PagedModel<Board> resources = new PagedModel<>(boards.getContent(), pageMetadata);
        //1.
        //PagedModel 객체 생성 시 따로 링크를 설정하지 않았다면 이와 같이 링크를 추가할 수 있다.
        //여기서는 각 Board마다 상세정보를 불러올 수 있는 링크만 추가했다.
        resources.add(linkTo(methodOn(BoardRestController.class).getBoards(pageable)).withSelfRel());
        return ResponseEntity.ok(resources);
    }

    //2. POST 요청에 대한 매핑 지원
    @PostMapping
    public ResponseEntity<?> postBoard(@RequestBody Board board) {
        board.setCreatedDateNow(); //서버 시간으로 생성된 날짜를 설정
        boardRepository.save(board);
        return new ResponseEntity<>("{}", HttpStatus.CREATED);
    }

    //2. PUT 요청에 대한 매핑 지원
    //어떤 board 객체를 수정할 것인지 idx 값을 지정해야 매핑된다.
    @PutMapping("/{idx}")
    public ResponseEntity<?> putBoard(@PathVariable("idx")Long idx, @RequestBody Board board) {
        Board persistBoard = boardRepository.getOne(idx);
        persistBoard.update(board);
        boardRepository.save(persistBoard);
        return new ResponseEntity<>("{}", HttpStatus.OK);
    }

    //2. DELETE 요청에 대한 매핑 지원
    //어떤 board 객체를 삭제할 것인지 idx 값을 지정해야 매핑된다.
    @DeleteMapping("/{idx}")
    public ResponseEntity<?> deleteBoard(@PathVariable("idx")Long idx) {
        boardRepository.deleteById(idx);
        return new ResponseEntity<>("{}", HttpStatus.OK);
    }
}
