package com.hoaxify.springbootreact;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class TestPage<T> implements Page<T> {

	long totalElements;
	int totalPages;
	int number;
	int numberOfElements;
	int size;
	boolean last;
	boolean first;
	boolean next;
	boolean previous;
	
	List<T> content;
	
	public TestPage() {
		super();
	}

	@Override
	public int getNumber() {
		// TODO Auto-generated method stub
		return number;
	}

	@Override
	public int getSize() {
		// TODO Auto-generated method stub
		return size;
	}

	@Override
	public int getNumberOfElements() {
		// TODO Auto-generated method stub
		return numberOfElements;
	}

	@Override
	public List<T> getContent() {
		// TODO Auto-generated method stub
		return content;
	}

	@Override
	public boolean hasContent() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Sort getSort() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isFirst() {
		// TODO Auto-generated method stub
		return first;
	}

	@Override
	public boolean isLast() {
		// TODO Auto-generated method stub
		return last;
	}

	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return next;
	}

	@Override
	public boolean hasPrevious() {
		// TODO Auto-generated method stub
		return previous;
	}

	@Override
	public Pageable nextPageable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Pageable previousPageable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<T> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getTotalPages() {
		// TODO Auto-generated method stub
		return totalPages;
	}

	@Override
	public long getTotalElements() {
		// TODO Auto-generated method stub
		return totalElements;
	}

	@Override
	public <U> Page<U> map(Function<? super T, ? extends U> converter) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isNext() {
		return next;
	}

	public void setNext(boolean next) {
		this.next = next;
	}

	public boolean isPrevious() {
		return previous;
	}

	public void setPrevious(boolean previous) {
		this.previous = previous;
	}

	public void setTotalElements(long totalElements) {
		this.totalElements = totalElements;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public void setNumberOfElements(int numberOfElements) {
		this.numberOfElements = numberOfElements;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void setLast(boolean last) {
		this.last = last;
	}

	public void setFirst(boolean first) {
		this.first = first;
	}

	public void setContent(List<T> content) {
		this.content = content;
	}
	
	
	

}
