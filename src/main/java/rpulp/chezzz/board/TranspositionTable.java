package rpulp.chezzz.board;

public final class TranspositionTable {
    public static final class Entry {
        public Entry() {
            depth_ = -1;
        }

        public short score_;
        public byte depth_;
        public long hash_;
        public String signature_;
    }

    public TranspositionTable() {
        for (int i = 0; i != entries_.length; ++i) {
            entries_[i] = new EntryLst();
        }

        clear();
    }

    public void clear() {
        size_ = 0;
        for (int i = 0; i != entries_.length; ++i) {
            entries_[i].size_ = 0;
        }
    }

    public int size() {
        return size_;
    }

    public boolean find(final long hash, final Entry res) {
        final int idx = ((int) (hash)) & SIZE_MASK;
        final EntryLst entries = entries_[idx];
        if (entries.size_ == 0) {
            return false;
        }

        for (int i = 0; i != entries.size_; ++i) {
            final Entry entry = entries.entries_[i];

            if (hash == entry.hash_) {
                res.score_ = entry.score_;
                res.depth_ = entry.depth_;
                res.signature_ = entry.signature_;
                return true;
            }
        }
        return false;
    }

    public void put(final long hash, final short score, final int depth, final String signature) {
        final int idx = ((int) (hash)) & SIZE_MASK;
        final EntryLst entries = entries_[idx];
        for (int i = 0; i != entries.size_; ++i) {
            final Entry entry = entries.entries_[i];
            if (hash == entry.hash_) {
                entry.score_ = score;
                entry.depth_ = (byte) depth;
                entry.signature_ = signature;
                return;
            }
        }
        if (entries.add(hash, score, depth, signature)) {
            ++size_;
        }
    }

    private final static int SIZE_LOG2 = 16;
    private final static int SIZE = 1 << SIZE_LOG2;
    private final static int SIZE_MASK = SIZE - 1;

    private final EntryLst[] entries_ = new EntryLst[SIZE];

    private int size_;

    private static final int ENTRY_LST_MAX_SIZE = 8;
    private static final int ENTRY_LST_INIT_SIZE = 8;

    private static final class EntryLst {
        EntryLst() {
            for (int i = 0; i != entries_.length; ++i) {
                entries_[i] = new Entry();
            }
        }

        boolean add(final long hash, final short score, final int depth, final String signature) {
            if (size_ == entries_.length) {
                final int new_size = ((size_ * 3) >>> 1) + 1;
                if (size_ > ENTRY_LST_MAX_SIZE) {
                    return false;
                }
                final Entry[] old_entries = entries_;
                entries_ = new Entry[new_size];
                System.arraycopy(old_entries, 0, entries_, 0, old_entries.length);
                for (int i = size_; i != entries_.length; ++i) {
                    entries_[i] = new Entry();
                }
            }

            final Entry entry = entries_[size_];
            entry.hash_ = hash;
            entry.score_ = score;
            entry.depth_ = (byte) depth;
            entry.signature_ = signature;
            ++size_;

            return true;
        }

        int size_;
        Entry[] entries_ = new Entry[ENTRY_LST_INIT_SIZE];
    }
}
