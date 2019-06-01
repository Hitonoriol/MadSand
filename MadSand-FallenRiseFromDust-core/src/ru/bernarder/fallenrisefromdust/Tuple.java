package ru.bernarder.fallenrisefromdust;

public class Tuple<T1, T2> {
	T1 l;
	T2 r;
	
	Tuple (T1 l, T2 r) {
		set(l, r);
	}
	
	Tuple<T1, T2> set(T1 l, T2 r) {
		this.l = l;
		this.r = r;
		return this;
	}
}
