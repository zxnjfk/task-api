package com.example.task_api.dto;
import java.util.List;
import org.springframework.data.domain.Page;


public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public PageResponse(Page<T> source){
        this.content=source.getContent();
        this.page=source.getNumber();
        this.size=source.getSize();
        this.totalPages=source.getTotalPages();
        this.totalElements=source.getTotalElements();


    }

    public List<T> getContent(){
        return content;
    }

    public int getPage(){
        return page;
    }

    public int getSize(){
        return size;
    }

    public long getTotalElements(){
        return totalElements;

    }

    public int getTotalPages(){
        return totalPages;
    }
}
