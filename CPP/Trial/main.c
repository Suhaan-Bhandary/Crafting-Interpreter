#include <stdio.h>
#include <stdlib.h>

struct DoublyLinkedList {
  int data;
  struct DoublyLinkedList *next, *pre;
};

void insertNode(struct DoublyLinkedList **head, int data) {
  printf("Inserting Node: %d\n", data);

  struct DoublyLinkedList *node = malloc(sizeof(struct DoublyLinkedList));
  node->data = data;
  node->next = NULL;
  node->pre = NULL;

  // Check if head is NULL of not
  if (!(*head)) {
    *head = node;
    return;
  }

  // We insert the node at the end of the list
  struct DoublyLinkedList *curr = *head;

  while (curr->next) {
    curr = curr->next;
  }

  node->pre = curr;
  curr->next = node;
}

// Delete the first node having the data as given target
void deleteNode(struct DoublyLinkedList **head, int target) {
  printf("Deleting Node: %d\n", target);

  if (!(*head))
    return;

  // If the head is the node we have to delete
  if ((*head)->data == target) {
    struct DoublyLinkedList *curr = *head;

    *head = curr->next;
    (*head)->pre = NULL;

    // clear the node
    free(curr);
    return;
  }

  // Other non head
  struct DoublyLinkedList *curr = *head, *pre = NULL;

  while (curr) {
    if (curr->data == target) {
      pre->next = curr->next;
      if (curr->next)
        curr->next->pre = pre;

      free(curr);
      return;
    }

    pre = curr;
    curr = curr->next;
  }
}

void displayList(struct DoublyLinkedList *head) {
  printf("Doubly LinkedList: ");
  struct DoublyLinkedList *curr = head;

  while (curr) {
    if (curr->next)
      printf("%d -> ", curr->data);
    else
      printf("%d", curr->data);

    curr = curr->next;
  }
}

int main() {
  printf("Hello World!!\n");

  struct DoublyLinkedList *head = NULL;

  // Inserting 3 nodes
  insertNode(&head, 1);
  insertNode(&head, 2);
  insertNode(&head, 3);
  insertNode(&head, 4);
  insertNode(&head, 5);
  insertNode(&head, 6);
  insertNode(&head, 7);

  // Delete nodes
  deleteNode(&head, 3);
  deleteNode(&head, 1);
  deleteNode(&head, 7);

  // Displaying the nodes
  displayList(head);

  return 0;
}
