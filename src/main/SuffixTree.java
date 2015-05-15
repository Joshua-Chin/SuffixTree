package main;

import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

public class SuffixTree {

	private final StringBuffer text = new StringBuffer();
	private final Node root = new Node(0, 0);
	private Node activeNode = root;
	private int remainder = 0; // How many suffixes we need to add
	private int activeLength = 0; // How far along the edge the active node is
	private int activeEdge = 0; // Index of an instance of the first character
								// of the edge within the text

	public SuffixTree() {
	}

	public SuffixTree(String s) {
		for (int i = 0; i < s.length(); i++) {
			addChar(s.charAt(i));
		}
		addChar('$');
	}

	class Node implements CharSequence {
		protected int start, end = Integer.MAX_VALUE;
		protected Node suffixLink = root;
		private Map<Character, Node> children = new HashMap<>();

		public Node(int start) {
			this.start = start;
		}

		public Node(int start, int end) {
			this.start = start;
			this.end = end;
		}

		@Override
		public int length() {
			return Math.abs(Math.min(text.length(), end) - start);
		}

		@Override
		public char charAt(int index) {
			return text.charAt(start + index);
		}

		public boolean hasChild(char c) {
			return children.containsKey(c);
		}

		public boolean hasChild(int i) {
			return children.containsKey(text.charAt(i));
		}

		public Node getChild(char c) {
			return children.get(c);
		}

		public Node getChild(int i) {
			return children.get(text.charAt(i));
		}

		public Collection<Node> children() {
			return children.values();
		}

		public void addChild(Node n) {
			children.put(n.charAt(0), n);
		}

		@Override
		public String toString() {
			return text.substring(start, Math.min(text.length(), end));
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			return new Node(this.start + start, this.start + end);
		}

	}

	public void addChar(char c) {
		text.append(c);
		remainder++;
		Node lastNode = null;

		while (remainder > 0) {

			if (activeLength == 0) {
				activeEdge = text.length() - 1;
			}

			if (!activeNode.hasChild(activeEdge)) {
				// If the current node does not have a child corresponding to
				// the active edge, add it

				activeNode.addChild(new Node(activeEdge));

				if (lastNode != null) {
					lastNode.suffixLink = activeNode;
					lastNode = null;
				}

			} else {

				Node next = activeNode.getChild(activeEdge);
				if (walkdown(next)) {
					continue;
				}

				if (next.charAt(activeLength) == c) {
					if (lastNode != null && activeNode != root) {
						lastNode.suffixLink = activeNode;
						lastNode = null;
					}
					activeLength++;
					break;
				}

				// We split next at the active length
				Node split = new Node(next.start, next.start + activeLength);
				activeNode.addChild(split);

				split.addChild(new Node(text.length() - 1));
				next.start += activeLength;
				split.addChild(next);

				if (lastNode != null) {
					lastNode.suffixLink = split;
				}
				lastNode = split;
			}

			remainder--;
			if (activeNode == root) {
				if (activeLength > 0) {
					activeLength--;
					activeEdge = text.length() - remainder;
				}
			} else {
				activeNode = activeNode.suffixLink;
			}
		}
	}

	private boolean walkdown(Node next) {
		if (activeLength >= next.length()) {
			activeEdge += next.length();
			activeLength -= next.length();
			activeNode = next;
			return true;
		}
		return false;
	}

	public boolean contains(String s) {
		Node n = root;
		int index = 0;
		while (true) {
			n = n.children.get(s.charAt(index));
			String sub = n.toString();
			if (sub.length() < s.length() - index) {
				if (!s.startsWith(sub, index)) {
					return false;
				} else {
					index += sub.length();
				}
			} else {
				return sub.startsWith(s.substring(index));
			}
		}
	}

	public String longestRepeatedSubstring() {
		return longestRepeatedSubstring(root).data.stream().collect(
				Collectors.joining());

	}

	private class LRSRecord {
		public int size;
		public Deque<String> data;

		public LRSRecord(int size, Deque<String> data) {
			this.size = size;
			this.data = data;
		}
	}

	private LRSRecord longestRepeatedSubstring(Node n) {
		LRSRecord out = n.children().stream()
				.map((x) -> longestRepeatedSubstring(x))
				.max((a, b) -> Integer.compare(a.size, b.size))
				.orElse(new LRSRecord(0, new LinkedList<>()));
		if (n.children().size() != 0) {
			out.size += n.length();
			out.data.addFirst(n.toString());
		}
		return out;
	}

	@Override
	public String toString() {
		return "SuffixTree(\"" + text.toString() + "\")";
	}

	public static void main(String[] args) {
		SuffixTree s = new SuffixTree("ATCGATCGA$");
		System.out.println(s);
		System.out.println(s.longestRepeatedSubstring());
	}

}
