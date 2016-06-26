package com.prosper.chasing.common.queue;

import com.lmax.disruptor.RingBuffer;

public class Producer {
    
    private RingBuffer<QueueObject> ringBuffer;

    public Producer(RingBuffer<QueueObject> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }
    
    public void produce(Object o) {
        long sequence = ringBuffer.next();
        try {
            QueueObject queueObject = ringBuffer.get(sequence); 
            queueObject.set(o); 
        } finally {
            ringBuffer.publish(sequence);
        }
    }
}
