package com.alexshilkin.rxcombinelatesttest

import android.os.Handler
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import io.reactivex.internal.operators.flowable.FlowableCombineLatest
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.DisposableSubscriber
import net.bytebuddy.implementation.bytecode.Throw
import org.junit.Test

import org.junit.Assert.*
import org.mockito.Mockito
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean


class FlowableCombineLatestTest {

    @Test
    fun `Given sourceFirst one emit and sourceSecond thow emit`() {
        //given
        val sourceFirst = Flowable.just(1)

        val sourceSecond = Flowable.just(2, 3)

        Flowable.combineLatest(sourceFirst, sourceSecond,
            BiFunction<Int, Int, Pair<Int, Int>> { t1, t2 -> Pair(t1, t2) })
            .subscribe({
                println("OnNext: first = ${it.first}, second = ${it.second}")
            }, {
                println("OnError: ${it.message}")
            })
    }

    @Test
    fun `Given not delay sources Should `() {
        //given
        val sourceFirst = Flowable.just(1, 2)

        val sourceSecond = Flowable.just(3, 4)

        Flowable.combineLatest(sourceFirst, sourceSecond,
            BiFunction<Int, Int, Pair<Int, Int>> { t1, t2 -> Pair(t1, t2) })
            .subscribe({
                println("OnNext: first = ${it.first}, second = ${it.second}")
            }, {
                println("OnError: ${it.message}")
            })
    }

    @Test
    fun `Given item emit delay sources Should `() {
        //given
        val sourceFirst = Flowable.interval(1, TimeUnit.SECONDS)

        val sourceSecond = Flowable.interval(1, TimeUnit.SECONDS)

        Flowable.combineLatest(sourceFirst, sourceSecond,
            BiFunction<Long, Long, Pair<Long, Long>> { t1, t2 -> Pair(t1, t2) })
            .subscribe({
                println("OnNext: first = ${it.first}, second = ${it.second}")
            }, {
                println("OnError: ${it.message}")
            })

        Thread.sleep(2000)
    }

    @Test
    fun `Given request 1 delay sources and backpressure default Should on next one call`() {
        //given
        val sourceFirst = Flowable.interval(1, TimeUnit.SECONDS).onBackpressureBuffer()

        val sourceSecond = Flowable.interval(1, TimeUnit.SECONDS).onBackpressureBuffer()

        val subscriber: DisposableSubscriber<Pair<Long, Long>> = object : DisposableSubscriber<Pair<Long, Long>>() {
            override fun onStart() {
                request(1)
            }

            override fun onComplete() {
            }

            override fun onNext(t: Pair<Long, Long>?) {
                println("OnNext: first = ${t?.first}, second = ${t?.second}")
            }

            override fun onError(t: Throwable?) {
            }
        }

        Flowable.combineLatest(sourceFirst, sourceSecond,
            BiFunction<Long, Long, Pair<Long, Long>> { t1, t2 -> Pair(t1, t2) })
            .subscribe(subscriber)

        Thread.sleep(2000)
    }

    @Test
    fun `Given request 1 start and onNext delay sources and backpressure default Should on next two call`() {
        //given
        val sourceFirst = Flowable.interval(1, TimeUnit.SECONDS).onBackpressureBuffer()

        val sourceSecond = Flowable.interval(1, TimeUnit.SECONDS).onBackpressureBuffer()

        val subscriber: DisposableSubscriber<Pair<Long, Long>> = object : DisposableSubscriber<Pair<Long, Long>>() {
            override fun onStart() {
                request(1)
            }

            override fun onComplete() {
            }

            override fun onNext(t: Pair<Long, Long>?) {
                println("OnNext: first = ${t?.first}, second = ${t?.second}")
                request(1)
            }

            override fun onError(t: Throwable?) {
            }
        }

        Flowable.combineLatest(sourceFirst, sourceSecond,
            BiFunction<Long, Long, Pair<Long, Long>> { t1, t2 -> Pair(t1, t2) })
            .subscribe(subscriber)

        Thread.sleep(5000)
    }

    @Test
    fun `Given request 2 start and onNext delay sources and backpressure default Should on next two call`() {
        //given

        val sourceFirst = Flowable.interval(1, TimeUnit.SECONDS).onBackpressureBuffer()

        val sourceSecond = Flowable.interval(1, TimeUnit.SECONDS).onBackpressureBuffer()

        val atomicBoolean = AtomicBoolean(true)

        val subscriber: DisposableSubscriber<Pair<Long, Long>> = object : DisposableSubscriber<Pair<Long, Long>>() {
            override fun onStart() {
                request(1)
            }

            override fun onComplete() {
            }

            override fun onNext(t: Pair<Long, Long>?) {
                println("OnNext: first = ${t?.first}, second = ${t?.second}")
                if (atomicBoolean.getAndSet(false)) {
                    request(1)
                }
            }

            override fun onError(t: Throwable?) {
            }
        }

        Flowable.combineLatest(sourceFirst, sourceSecond,
            BiFunction<Long, Long, Pair<Long, Long>> { t1, t2 -> Pair(t1, t2) })
            .subscribe(subscriber)

        Thread.sleep(5000)
    }

    @Test
    fun `Given request 1 start and onNext delay sources and backpressure default and add state true false Should on next two call`() {
        //given

        val sourceFirst = Flowable.interval(1, TimeUnit.SECONDS).onBackpressureBuffer()

        val sourceSecond = Flowable.interval(1, TimeUnit.SECONDS).onBackpressureBuffer()

        val state = AtomicBoolean(true)

        val atomicBoolean = AtomicBoolean(true)

        val subscriber: DisposableSubscriber<Pair<Long, Long>> = object : DisposableSubscriber<Pair<Long, Long>>() {
            override fun onStart() {
                request(1)
            }

            override fun onComplete() {
            }

            override fun onNext(t: Pair<Long, Long>?) {
                println("OnNext: first = ${t?.first}, second = ${t?.second}")
                if (state.get()) {
                    if (atomicBoolean.getAndSet(false)) {
                        state.set(false)
                        atomicBoolean.set(true)
                        request(1)
                    }
                } else {
                    if (atomicBoolean.getAndSet(false)) {
                        atomicBoolean.set(true)
                        state.set(true)
                        request(1)
                    }
                }

            }

            override fun onError(t: Throwable?) {
            }
        }

        Flowable.combineLatest(sourceFirst, sourceSecond,
            BiFunction<Long, Long, Pair<Long, Long>> { t1, t2 -> Pair(t1, t2) })
            .subscribe(subscriber)

        Thread.sleep(5000)
    }
}


