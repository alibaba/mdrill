package com.alipay.bluewhale.core.time;

import java.util.PriorityQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

//FIXME 不在使用的类，建议删除
@Deprecated
public class StormTimer {

	private Thread timerThread;
	private PriorityQueue queue;
	private AtomicBoolean  active;
	private Object lock;
	private Semaphore cancelNotifier;

	public StormTimer(Thread timerThread, PriorityQueue queue, AtomicBoolean active,
			Object lock, Semaphore cancelNotifier) {
		this.timerThread = timerThread;
		this.queue = queue;
		this.active = active;
		this.lock = lock;
		this.cancelNotifier = cancelNotifier;
	}

	public Thread getTimerThread() {
		return timerThread;
	}

	public void setTimerThread(Thread timerThread) {
		this.timerThread = timerThread;
	}

	public PriorityQueue getQueue() {
		return queue;
	}

	public void setQueue(PriorityQueue queue) {
		this.queue = queue;
	}

	public boolean isActive() {
		return active.get();
	}

	public void setActive(AtomicBoolean active) {
		this.active = active;
	}

	public Object getLock() {
		return lock;
	}

	public void setLock(Object lock) {
		this.lock = lock;
	}

	public Semaphore getCancelNotifier() {
		return cancelNotifier;
	}

	public void setCancelNotifier(Semaphore cancelNotifier) {
		this.cancelNotifier = cancelNotifier;
	}

}
