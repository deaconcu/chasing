package com.prosper.chasing.common.queue;

import java.util.concurrent.Executors;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

public class DisruptorQueue {
    
    private Producer producer;
    
    public DisruptorQueue(int capacity, EventHandler<QueueObject> eventHandler) {
        QueueObjectFactory factory = new QueueObjectFactory();
        
        int bufferSize = capacity;
        Disruptor<QueueObject> disruptor = new Disruptor<>(factory, bufferSize, Executors.defaultThreadFactory());
        
        disruptor.handleEventsWith(eventHandler);
        disruptor.start();
        
        RingBuffer<QueueObject> ringBuffer = disruptor.getRingBuffer();
        producer = new Producer(ringBuffer);
    }

    public Producer getProducer() {
        return producer;
    }
}
