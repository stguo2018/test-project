package com.ews.stguo.testproject.datastructure;

import org.junit.Test;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class ListNodeTest {

    class ListNode {
        ListNode(int val) {
            this.val = val;
        }
        int val;
        ListNode next;
    }

    @Test
    public void testReverseListNode() {
        ListNode sourceListNode = new ListNode(1);
        ListNode cNode = sourceListNode;
        for (int i = 2; i <= 5; i++) {
            cNode.next = new ListNode(i);
            cNode = cNode.next;
        }
        ListNode reverseListNode = reverseList(sourceListNode);
        if (reverseListNode != null) {
            System.out.print("[" + reverseListNode.val);
            while (reverseListNode.next != null) {
                reverseListNode = reverseListNode.next;
                System.out.print("," + reverseListNode.val);
            }
            System.out.print("]\n");
        }
    }

    private ListNode reverseList(ListNode head) {
        ListNode reverse = null;
        while (head != null) {
            ListNode tmpNode = new ListNode(head.val);
            tmpNode.next = reverse;
            reverse = tmpNode;
            head = head.next;
        }
        return reverse;
    }

}
