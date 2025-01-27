/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#pragma once

#include <nativehelper/JNIHelp.h>

#include "JniConstants.h"

/**
 * ScopedBytesRO and ScopedBytesRW attempt to paper over the differences between byte[]s and
 * ByteBuffers. This in turn helps paper over the differences between non-direct ByteBuffers backed
 * by byte[]s, direct ByteBuffers backed by bytes[]s, and direct ByteBuffers not backed by byte[]s.
 * (On Android, this last group only contains MappedByteBuffers.)
 */
template<bool readOnly>
class ScopedBytes {
public:
    ScopedBytes(JNIEnv* env, jobject object)
    : mEnv(env), mObject(object), mByteArray(nullptr), mPtr(nullptr)
    {
        if (mObject == nullptr) {
            jniThrowNullPointerException(mEnv);
        } else {
            jclass byteArrayClass = JniConstants::GetPrimitiveByteArrayClass(env);
            if (mEnv->IsInstanceOf(mObject, byteArrayClass)) {
                mByteArray = reinterpret_cast<jbyteArray>(mObject);
                mPtr = mEnv->GetByteArrayElements(mByteArray, nullptr);
            } else {
                mPtr = reinterpret_cast<jbyte*>(mEnv->GetDirectBufferAddress(mObject));
            }
        }
    }

    ~ScopedBytes() {
        if (mByteArray != nullptr) {
            mEnv->ReleaseByteArrayElements(mByteArray, mPtr, readOnly ? JNI_ABORT : 0);
        }
    }

private:
    JNIEnv* const mEnv;
    const jobject mObject;
    jbyteArray mByteArray;

protected:
    jbyte* mPtr;

private:
    DISALLOW_COPY_AND_ASSIGN(ScopedBytes);
};

class ScopedBytesRO : public ScopedBytes<true> {
public:
    ScopedBytesRO(JNIEnv* env, jobject object) : ScopedBytes<true>(env, object) {}
    const jbyte* get() const {
        return mPtr;
    }
};

class ScopedBytesRW : public ScopedBytes<false> {
public:
    ScopedBytesRW(JNIEnv* env, jobject object) : ScopedBytes<false>(env, object) {}
    jbyte* get() {
        return mPtr;
    }
};

