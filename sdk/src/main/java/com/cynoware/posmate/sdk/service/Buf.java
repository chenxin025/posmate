package com.cynoware.posmate.sdk.service;


public class Buf {
    private byte[] buffer_;
    int start_;
    int end_;
    int bufferSize_;

    private void ASSERT(boolean result){
    }

    public Buf(int maxSize){
        start_ = 0;
        end_ = 0;
        bufferSize_ = maxSize + 1;
        buffer_ = new byte[bufferSize_];
    }

    public int size(){
        int size = end_ - start_;
        ASSERT(end_ < bufferSize_);
        ASSERT(start_ < bufferSize_);

        if(size < 0)
            return bufferSize_ + size;
        else
            return size;
    }

    public int free(){
        int usedSize = size();
        int freeSize = bufferSize_ - 1 - usedSize;
        ASSERT(freeSize < bufferSize_);
        return freeSize;
    }

    public int pushBack(byte[] buffer, int offset, int size){
        int end;
        int freeSize = free();
        int copySize = (freeSize < size ? freeSize : size);

        int toEndSize = bufferSize_ - end_;
        int copy1stSize = (copySize < toEndSize ? copySize : toEndSize);
        int copy2ndSize = copySize - copy1stSize;

        System.arraycopy(buffer, offset, buffer_, end_, copy1stSize);
        end = end_ + copy1stSize;
        if(end >= bufferSize_){
            end -= bufferSize_;
            ASSERT(end < bufferSize_);
        }
        end_ = end;

        if(copy2ndSize > 0){
            System.arraycopy(buffer, offset + copy1stSize, buffer_, end_, copy2ndSize);
            ASSERT(end_ == 0);
            end_ += copy2ndSize;
        }

        return copySize;
    }

    public int pushFront(byte[] buffer, int offset, int size){
        int start;
        int freeSize = free();
        int copySize = (freeSize < size ? freeSize : size);

        int toBeginSize = start_;
        int copy1stSize = (copySize < toBeginSize ? copySize : toBeginSize);
        int copy2ndSize = copySize - copy1stSize;

        start = start_ - copy1stSize;
        System.arraycopy(buffer, offset + copy2ndSize, buffer_, start, copy1stSize);
        start_ = start;

        if(copy2ndSize > 0){
            ASSERT(start_ == 0);
            start = bufferSize_ - copy2ndSize;
            System.arraycopy(buffer, offset, buffer_, start, copy2ndSize);
            start_ = start;
        }

        return copySize;
    }

    public int popFront(byte[] buffer, int offset, int size){
        int start;
        int usedSize = size();
        int copySize = (usedSize < size ? usedSize : size);

        int toEndSize = bufferSize_ - start_;
        int copy1stSize = (copySize < toEndSize ? copySize : toEndSize);
        int copy2ndSize = copySize - copy1stSize;

        System.arraycopy(buffer_, start_, buffer, offset, copy1stSize);
        start = start_ + copy1stSize;
        if(start >= bufferSize_){
            start -= bufferSize_;
            ASSERT(start_ < bufferSize_);
        }
        start_ = start;

        if(copy2ndSize > 0){
            ASSERT(start_ == 0);
            System.arraycopy(buffer_, start_, buffer, offset + copy1stSize, copy2ndSize);
            start_ += copy2ndSize;
        }

        return copySize;
    }

    public int popBack(byte[] buffer, int offset, int size){
        int end;
        int usedSize = size();
        int copySize = (usedSize < size ? usedSize : size);

        int toBeginSize = end_;
        int copy1stSize = (copySize < toBeginSize ? copySize : toBeginSize);
        int copy2ndSize = copySize - copy1stSize;

        end = end_ - copy1stSize;
        System.arraycopy(buffer_, end, buffer, offset + copy2ndSize, copy1stSize);
        end_ = end;

        if(copy2ndSize > 0){
            ASSERT(end_ == 0);
            end = bufferSize_ - copy2ndSize;
            System.arraycopy(buffer_, end, buffer, offset, copy2ndSize);
            end_ = end;
        }

        return copySize;
    }


    public int push1Front(byte ch){
        int start = start_;
        if(start == 0) start = bufferSize_ - 1;
        else --start;
        if(start != end_){
            buffer_[start] = ch;
            start_ = start;
            return 1;
        }
        else
            return 0;
    }

    public int push1Back(byte ch){
        int end = end_;
        ++end;
        if(end >= bufferSize_) end = 0;
        if(end != start_){
            buffer_[end_] = ch;
            end_ = end;
            return 1;
        }
        else
            return 0;
    }

    public int pop1Front(Byte ch){
        if(start_ != end_){
            int start = start_;
            ch = buffer_[start];
            ++start;
            if(start >= bufferSize_) start = 0;
            start_ = start;
            return 1;
        }
        else
            return 0;
    }

    public int pop1Back(Byte ch){
        if(start_ != end_){
            int end = end_;
            if(end == 0) end = bufferSize_ - 1;
            else --end;
            ch = buffer_[end];
            end_ = end;
            return 1;
        }
        else
            return 0;
    }

    public byte bufGetAt(int index){
        index = start_ + index;
        while(index > bufferSize_) index -= bufferSize_;
        return buffer_[index];
    }

    public void bufSetAt(int index, byte ch){
        index = start_ + index;
        while(index > bufferSize_) index -= bufferSize_;
        buffer_[index] = ch;
    }

    public void bufClear(){
        start_ = 0;
        end_ = 0;
    }

    public void bufClearByStart(){
        start_ = end_;
    }
    public void bufClearByEnd(){
        end_ = start_;
    }
}
