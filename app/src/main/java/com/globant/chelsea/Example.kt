package com.globant.chelsea

import android.util.Log
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.*

class Example {

    private val valueSubject = ReplaySubject.create<String>()

    init {
        val valueSubject = PublishSubject.create<String>()
        valueSubject.onNext("Item 1")
    }

    fun getValue(): Observable<String> {
        return valueSubject.hide()
    }

    fun x() {
        val subject = PublishSubject.create<String>()
        val value: Observable<String> = subject
        value.subscribe { doWork("Consumer 1: $it") }
        value.subscribe { doWork("Consumer 2: $it") }
        subject.onNext("A")
        value.subscribe { doWork("Consumer 3: $it") }
        subject.onNext("A")
    }

    private fun doWork(s: String) = Unit
}

class Consumer {

    private val example = Example()

    fun execute() {
        val valueObservable: Observable<String> = example.getValue() //
        valueObservable.subscribe {
            if (valueObservable is Subject) valueObservable.onNext("Item 2")
            doSomething(it)
        }
    }

    private fun doSomething(s: String) {
        Log.d("Subjects", "value: $s")
    }


}

class UnicastExample {

    val TAG = "UnicastSubject"
    val subject = UnicastSubject.create<Int>()
    val observable = subject.hide()

    fun unicastSubjectExample() {
        subject.onNext(1)
        subject.onNext(2)
        observable // after some emissions
            .subscribe { actionOnNext(1, it) }
        observable // after the first observer subscribes
            .subscribe({ actionOnNext(2, it) }, { actionError(2) })
        subject.onNext(3)
        subject.onNext(4)
        subject.onComplete()
        observable // after all emissions and completion
            .subscribe({ actionOnNext(3, it) }, { actionError(3) })
    }

    private fun actionError(observerIndex: Int) =
        Log.d(TAG, "Observer $observerIndex, error")

    private fun actionOnNext(observerIndex: Int, value: Int) =
        Log.d(TAG, "Observer $observerIndex, Item: $value")
}


class AsyncExample {

    val TAG = "AsyncSubject"
    val subject = AsyncSubject.create<Int>()
    val observable = subject.hide()

    fun asyncSubjectExample() {
        observable // before any emission
            .doOnComplete { actionCompleted(1) }
            .subscribe { actionOnNext(1, it) }
        subject.onNext(1)
        subject.onNext(2)
        subject.onNext(3)
        subject.onNext(4)
        subject.onNext(5)
        subject.onComplete()
        observable // after all emissions and completion
            .doOnComplete { actionCompleted(2) }
            .subscribe { actionOnNext(2, it) }
    }

    private fun actionCompleted(observerIndex: Int) =
        Log.d(TAG, "Observer $observerIndex, completed")

    private fun actionOnNext(observerIndex: Int, value: Int) =
        Log.d(TAG, "Observer $observerIndex, Item: $value")
}


class PublishExample {

    val TAG = "PublishSubject"
    val subject = PublishSubject.create<Int>()
    val observable = subject.hide()

    fun publishSubjectExample() {
        observable // before any emission
            .doOnComplete { actionCompleted(1) }
            .subscribe { actionOnNext(1, it) }
        subject.onNext(1)
        subject.onNext(2)
        observable // after emissions started
            .doOnComplete { actionCompleted(2) }
            .subscribe { actionOnNext(2, it) }
        subject.onNext(3)
        subject.onNext(4)
        subject.onComplete()
        observable // after all emissions and completion
            .doOnComplete { actionCompleted(3) }
            .subscribe { actionOnNext(3, it) }
    }

    private fun actionCompleted(observerIndex: Int) =
        Log.d(TAG, "Observer $observerIndex, completed")

    private fun actionOnNext(observerIndex: Int, value: Int) =
        Log.d(TAG, "Observer $observerIndex, Item: $value")
}

class BehaviorExample {

    val TAG = "BehaviorSubject"
    val subject = BehaviorSubject.createDefault(0)
    val observable = subject.hide()

    fun behaviorSubjectExample() {
        observable // before any emission
            .subscribe { actionOnNext(1, it) }
        subject.onNext(1)
        subject.onNext(2)
        observable // after emissions started
            .subscribe { actionOnNext(2, it) }
        subject.onNext(3)
        subject.onNext(4)
        subject.onComplete()
        subject.onNext(5) // no effect
        observable // after all emissions and completion
            .subscribe { actionOnNext(3, it) }
    }

    private fun actionCompleted(observerIndex: Int) =
        Log.d(TAG, "Observer $observerIndex, completed")

    private fun actionOnNext(observerIndex: Int, value: Int) =
        Log.d(TAG, "Observer $observerIndex, Item: $value")
}


class ReplayExample {

    val TAG = "ReplaySubject"
    val subject = ReplaySubject.create<Int>()
    val observable = subject.hide()

    fun replaySubjectExample() {
        observable // before any emission
            .doOnComplete { actionCompleted(1) }
            .subscribe { actionOnNext(1, it) }
        subject.onNext(1)
        subject.onNext(2)
        observable // after emissions started
            .doOnComplete { actionCompleted(2) }
            .subscribe { actionOnNext(2, it) }
        subject.onNext(3)
        subject.onNext(4)
        subject.onComplete()
        subject.onNext(5) // no effect
        observable // after all emissions and completion
            .doOnComplete { actionCompleted(3) }
            .subscribe { actionOnNext(3, it) }
    }

    private fun actionCompleted(observerIndex: Int) =
        Log.d(TAG, "Observer $observerIndex, completed")

    private fun actionOnNext(observerIndex: Int, value: Int) =
        Log.d(TAG, "Observer $observerIndex, Item: $value")
}
