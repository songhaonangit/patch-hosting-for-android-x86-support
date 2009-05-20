/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.dx.cf.code;

import com.android.dx.rop.cst.CstUtf8;
import com.android.dx.rop.type.Type;
import com.android.dx.rop.code.LocalItem;
import com.android.dx.util.FixedSizeList;

/**
 * List of "local variable" entries, which are the contents of
 * <code>LocalVariableTable</code> and <code>LocalVariableTypeTable</code>
 * attributes, as well as combinations of the two.
 */
public final class LocalVariableList extends FixedSizeList {
    /** non-null; zero-size instance */
    public static final LocalVariableList EMPTY = new LocalVariableList(0);

    /**
     * Returns an instance which is the concatenation of the two given
     * instances. The result is immutable.
     * 
     * @param list1 non-null; first instance
     * @param list2 non-null; second instance
     * @return non-null; combined instance
     */
    public static LocalVariableList concat(LocalVariableList list1,
                                           LocalVariableList list2) {
        if (list1 == EMPTY) {
            // easy case
            return list2;
        }

        int sz1 = list1.size();
        int sz2 = list2.size();
        LocalVariableList result = new LocalVariableList(sz1 + sz2);

        for (int i = 0; i < sz1; i++) {
            result.set(i, list1.get(i));
        }

        for (int i = 0; i < sz2; i++) {
            result.set(sz1 + i, list2.get(i));
        }

        result.setImmutable();
        return result;
    }

    /**
     * Returns an instance which is the result of merging the two
     * given instances, where one instance should have only type
     * descriptors and the other only type signatures. The merged
     * result is identical to the one with descriptors, except that
     * any element whose {name, index, start, length} matches an
     * element in the signature list gets augmented with the
     * corresponding signature. The result is immutable.
     * 
     * @param descriptorList non-null; list with descriptors
     * @param signatureList non-null; list with signatures
     * @return non-null; the merged result
     */
    public static LocalVariableList mergeDescriptorsAndSignatures(
            LocalVariableList descriptorList,
            LocalVariableList signatureList) {
        int signatureSize = signatureList.size();
        int descriptorSize = descriptorList.size();
        LocalVariableList result = new LocalVariableList(descriptorSize);

        for (int i = 0; i < descriptorSize; i++) {
            Item item = descriptorList.get(i);
            Item signatureItem = signatureList.itemToLocal(item);
            if (signatureItem != null) {
                CstUtf8 signature = signatureItem.getSignature();
                item = item.withSignature(signature);
            }
            result.set(i, item);
        }        

        result.setImmutable();
        return result;
    }

    /**
     * Constructs an instance.
     * 
     * @param count the number of elements to be in the list
     */
    public LocalVariableList(int count) {
        super(count);
    }

    /**
     * Gets the indicated item.
     * 
     * @param n &gt;= 0; which item
     * @return null-ok; the indicated item
     */
    public Item get(int n) {
        return (Item) get0(n);
    }

    /**
     * Sets the item at the given index.
     * 
     * @param n &gt;= 0, &lt; size(); which element
     * @param item non-null; the item
     */
    public void set(int n, Item item) {
        if (item == null) {
            throw new NullPointerException("item == null");
        }

        set0(n, item);
    }

    /**
     * Sets the item at the given index.
     * 
     * <p><b>Note:</b> At least one of <code>descriptor</code> or
     * <code>signature</code> must be passed as non-null.</p>
     * 
     * @param n &gt;= 0, &lt; size(); which element
     * @param startPc &gt;= 0; the start pc of this variable's scope
     * @param length &gt;= 0; the length (in bytecodes) of this variable's
     * scope
     * @param name non-null; the variable's name
     * @param descriptor null-ok; the variable's type descriptor
     * @param signature null-ok; the variable's type signature
     * @param index &gt;= 0; the variable's local index
     */
    public void set(int n, int startPc, int length, CstUtf8 name,
            CstUtf8 descriptor, CstUtf8 signature, int index) {
        set0(n, new Item(startPc, length, name, descriptor, signature, index));
    }

    /**
     * Gets the local variable information in this instance which matches
     * the given {@link com.android.dx.cf.code.LocalVariableList.Item}
     * in all respects but the type descriptor and signature, if any.
     * 
     * @param item non-null; local variable information to match
     * @return null-ok; the corresponding local variable information stored
     * in this instance, or <code>null</code> if there is no matching
     * information
     */
    public Item itemToLocal(Item item) {
        int sz = size();

        for (int i = 0; i < sz; i++) {
            Item one = (Item) get0(i);

            if ((one != null) && one.matchesAllButType(item)) {
                return one;
            }
        }

        return null;
    }

    /**
     * Gets the local variable information associated with a given address
     * and local index, if any. <b>Note:</b> In standard classfiles, a
     * variable's start point is listed as the address of the instruction
     * <i>just past</i> the one that sets the variable.
     * 
     * @param pc &gt;= 0; the address to look up
     * @param index &gt;= 0; the local variable index
     * @return null-ok; the associated local variable information, or
     * <code>null</code> if none is known
     */
    public Item pcAndIndexToLocal(int pc, int index) {
        int sz = size();

        for (int i = 0; i < sz; i++) {
            Item one = (Item) get0(i);

            if ((one != null) && one.matchesPcAndIndex(pc, index)) {
                return one;
            }
        }

        return null;
    }

    /**
     * Item in a local variable table.
     */
    public static class Item {
        /** &gt;= 0; the start pc of this variable's scope */
        private final int startPc;

        /** &gt;= 0; the length (in bytecodes) of this variable's scope */
        private final int length;

        /** non-null; the variable's name */
        private final CstUtf8 name;

        /** null-ok; the variable's type descriptor */
        private final CstUtf8 descriptor;

        /** null-ok; the variable's type signature */
        private final CstUtf8 signature;

        /** &gt;= 0; the variable's local index */
        private final int index;

        /**
         * Constructs an instance.
         * 
         * <p><b>Note:</b> At least one of <code>descriptor</code> or
         * <code>signature</code> must be passed as non-null.</p>
         * 
         * @param startPc &gt;= 0; the start pc of this variable's scope
         * @param length &gt;= 0; the length (in bytecodes) of this variable's
         * scope
         * @param name non-null; the variable's name
         * @param descriptor null-ok; the variable's type descriptor
         * @param signature null-ok; the variable's type signature
         * @param index &gt;= 0; the variable's local index
         */
        public Item(int startPc, int length, CstUtf8 name,
                CstUtf8 descriptor, CstUtf8 signature, int index) {
            if (startPc < 0) {
                throw new IllegalArgumentException("startPc < 0");
            }

            if (length < 0) {
                throw new IllegalArgumentException("length < 0");
            }

            if (name == null) {
                throw new NullPointerException("name == null");
            }

            if ((descriptor == null) && (signature == null)) {
                throw new NullPointerException(
                        "(descriptor == null) && (signature == null)");
            }

            if (index < 0) {
                throw new IllegalArgumentException("index < 0");
            }

            this.startPc = startPc;
            this.length = length;
            this.name = name;
            this.descriptor = descriptor;
            this.signature = signature;
            this.index = index;
        }

        /**
         * Gets the start pc of this variable's scope.
         * 
         * @return &gt;= 0; the start pc of this variable's scope
         */
        public int getStartPc() {
            return startPc;
        }

        /**
         * Gets the length (in bytecodes) of this variable's scope.
         * 
         * @return &gt;= 0; the length (in bytecodes) of this variable's scope
         */
        public int getLength() {
            return length;
        }

        /**
         * Gets the variable's type descriptor.
         *
         * @return null-ok; the variable's type descriptor
         */
        public CstUtf8 getDescriptor() {
            return descriptor;
        }

        /**
         * Gets the variable's LocalItem, a (name, signature) tuple
         *
         * @return null-ok; the variable's type descriptor
         */
        public LocalItem getLocalItem() {
            return LocalItem.make(name, signature);
        }

        /**
         * Gets the variable's type signature. Private because if you need this,
         * you want getLocalItem() instead.
         *
         * @return null-ok; the variable's type signature
         */
        private CstUtf8 getSignature() {
            return signature;
        }

        /**
         * Gets the variable's local index.
         * 
         * @return &gt;= 0; the variable's local index
         */
        public int getIndex() {
            return index;
        }

        /**
         * Gets the variable's type descriptor. This is a convenient shorthand
         * for <code>Type.intern(getDescriptor().getString())</code>.
         * 
         * @return non-null; the variable's type
         */
        public Type getType() {
            return Type.intern(descriptor.getString());
        }

        /**
         * Constructs and returns an instance which is identical to this
         * one, except that the signature is changed to the given value.
         * 
         * @param newSignature non-null; the new signature
         * @return non-null; an appropriately-constructed instance
         */
        public Item withSignature(CstUtf8 newSignature) {
            return new Item(startPc, length, name, descriptor, newSignature,
                    index);
        }

        /**
         * Gets whether this instance matches (describes) the given
         * address and index.
         * 
         * @param pc &gt;= 0; the address in question
         * @param index &gt;= 0; the local variable index in question
         * @return <code>true</code> iff this instance matches <code>pc</code>
         * and <code>index</code>
         */
        public boolean matchesPcAndIndex(int pc, int index) {
            return (index == this.index) &&
                (pc >= startPc) &&
                (pc < (startPc + length));
        }

        /**
         * Gets whether this instance matches (describes) the given
         * other instance exactly in all fields except type descriptor and
         * type signature.
         * 
         * @param other non-null; the instance to compare to
         * @return <code>true</code> iff this instance matches
         */
        public boolean matchesAllButType(Item other) {
            return (startPc == other.startPc)
                && (length == other.length)
                && (index == other.index)
                && name.equals(other.name);
        }
    }
}
