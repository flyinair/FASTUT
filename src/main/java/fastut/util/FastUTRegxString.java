package fastut.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class FastUTRegxString {

    private RegxStringImpl impl_;

    public FastUTRegxString(){
        impl_ = null;
    }

    public FastUTRegxString(String regx){
        impl_ = null;
        parseRegx(regx);
    }

    public void parseRegx(String regx) {
        parseRegx(regx, null);
    }

    public void parseRegx(String regx, Config config) {
        if (regx == null) {
            return;
        }
        if (impl_ == null) {
            impl_ = new RegxStringImpl();
        }
        impl_.parseRegx(regx, config);
    }

    public String regx() {
        return impl_ != null ? impl_.regx() : null;
    }

    public String randString() {
        return impl_ != null ? impl_.randString() : null;
    }

    public String lastString() {
        return impl_ != null ? impl_.lastString() : null;
    }

    public void replaceAndParse(String regx, String replaceTarget, String replaceStr) {
        replaceAndParse(regx, replaceTarget, replaceStr, null);
    }

    public void replaceAndParse(String regx, String replaceTarget, String replaceStr, Config config) {
        if (regx == null) {
            return;
        }
        if (impl_ == null) {
            impl_ = new RegxStringImpl();
        }
        impl_.parseRegx(regx.replace(replaceTarget, replaceStr), config);
    }

    public List<String> replaceAndParse(String regx, String replaceTarget, Set<String> replaceStrs) {
        List<String> results = new ArrayList<String>();
        for (String replaceStr : replaceStrs) {
            replaceAndParse(regx, replaceTarget, replaceStr, null);
            results.add(randString());
        }
        return results;
    }

    public static void main(String[] args) {
        FastUTRegxString regxStr = new FastUTRegxString();
        // regxStr.parseRegx("^(?<=\\w{4}abc\\d{3,5})\\d{3}");
        // regxStr.parseRegx("\\w{4}abc\\d{3,5}");
        // regxStr.parseRegx("\\#\\*(.*?)\\*\\#");
        // regxStr.replaceAndParse("\\#\\*(.*?)\\*\\#", ".*?", "DL|IC|RT");
        // for (int i = 0; i < 10; ++i) {
        // System.out.println(regxStr.randString());
        // }
        //System.out.println(regxStr.replaceAndParse("\\#\\*(.*?)\\*\\#", ".*?", Arrays.asList("DL", "IC", "RT")));
    }

    static class RegxStringImpl {

        String              regx_;
        String              str_;
        NodeBase            top_;

        static final String SEP = "  ";

        static String sep(int lvl) {
            StringBuilder ret = new StringBuilder();
            while (lvl-- > 0) {
                ret.append(SEP);
            }
            return ret.toString();
        }

        static void appendNode(Ret parent, NodeBase node) {
            if (node == null) {
                return;
            }
            if (parent.left == null) {
                parent.left = new Seq(node);
            } else {
                parent.left.appendNode(node);
            }
        }

        public RegxStringImpl(){
            top_ = null;
        }

        public void parseRegx(String regx, Config config) {
            uninit();
            regx_ = regx;
            if (regx_.length() == 0) {
                return;
            }
            Config def = new Config();
            ParseData pdata = new ParseData(config != null ? config : def);
            top_ = processSeq(pdata).left;
            if (top_ == null) {
                return;
            }
            NodeBase r = top_.optimize(pdata);
            if (r != null) {
                top_ = (r == NodeBase.REP_NULL ? null : r);
            }
        }

        public String regx() {
            return regx_;
        }

        public String randString() {
            if (top_ != null) {
                GenerateData gdata = new GenerateData();
                top_.randString(gdata);
                str_ = gdata.oss.toString();
            }
            return str_;
        }

        public String lastString() {
            return str_;
        }

        RegxStringImpl(RegxStringImpl regxStr){

        }

        void uninit() {
            if (top_ != null) {
                top_ = null;
            }
            str_ = "";
        }

        Ret processSeq(ParseData pdata) {
            Ret ret = new Ret();
            NodeBase cur = null;
            boolean begin = true;
            for (int e = regx_.length(); pdata.i_ < e; ++pdata.i_) {
                char ch = regx_.charAt(pdata.i_);
                if (begin) {
                    if (Tools.isBegin(ch)) {
                        cur = new Edge(ch);
                        continue;
                    }
                    begin = false;
                }
                if (Tools.isRepeat(ch) && cur != null) {
                    int r = cur.repeat(ch);
                    if (r != 0) {
                        if (r == 1) {
                            cur = new Repeat(cur, ch);
                        }
                        continue;
                    }
                }
                if (Tools.isRepeatBegin(ch)) {
                    cur = processRepeat(cur, pdata);
                    continue;
                }
                appendNode(ret, cur);
                ret.right = pdata.inEnds(ch);
                if (ret.right != 0) {
                    return ret;
                }
                if (Tools.isSelect(ch)) {
                    return processSelect(ret.left, pdata);
                }
                if (Tools.isEnd(ch)) {
                    cur = new Edge(ch);
                } else if (Tools.isAny(ch)) {
                    Charset set = new Charset("\n", false);
                    set.unique();
                    cur = set;
                } else if (Tools.isSetBegin(ch)) {
                    cur = processSet(pdata);
                } else if (Tools.isGroupBegin(ch)) {
                    cur = processGroup(pdata);
                } else if (Tools.isSlash(ch)) {
                    cur = processSlash(true, pdata).left;
                } else {
                    cur = new Text(ch);
                }
            }
            appendNode(ret, cur);
            return ret;
        }

        Ret processSlash(boolean bNode, ParseData pdata) {
            ++pdata.i_;
            Ret ret = new Ret(null, pdata.i_ < regx_.length() ? Tools.transSlash(regx_.charAt(pdata.i_)) : '\\');
            Charset set = null;
            switch (ret.right) {
                case 'd':
                    set = new Charset("0123456789", true);
                    break;
                case 'D':
                    set = new Charset("0123456789", false);
                    break;
                case 's':
                    set = new Charset("\t ", true);
                    break;
                case 'S':
                    set = new Charset("\t ", false);
                    break;
                case 'w': {
                    set = new Charset();
                    set.addRange('A', 'Z');
                    set.addRange('a', 'z');
                    set.addRange('0', '9');
                    set.addChar('_');
                    break;
                }
                case 'W': {
                    set = new Charset();
                    set.addRange('A', 'Z');
                    set.addRange('a', 'z');
                    set.addRange('0', '9');
                    set.addChar('_');
                    set.exclude();
                    break;
                }
                default:
                    ;
            }
            if (set != null) {
                set.unique();
                ret.left = set;
            } else if (bNode) {
                if (Tools.isDigit(ret.right)) {
                    int i = ret.right - '0';
                    if (i == 0) {
                        ret.right = 0;
                    } else if (i <= pdata.ref_) {
                        ret.left = new Ref(i);
                    }
                }
                if (ret.left == null) {
                    ret.left = new Text(ret.right);
                }
            }
            return ret;
        }

        NodeBase processSet(ParseData pdata) {
            int bak = pdata.i_++;
            Charset ret = new Charset();
            boolean begin = true;
            int prev = 0;
            for (int e = regx_.length(); pdata.i_ < e; ++pdata.i_) {
                int ch = regx_.charAt(pdata.i_);
                if (begin && Tools.isBegin(ch)) {
                    ret.exclude();
                    begin = false;
                    continue;
                }
                if (Tools.isDash(ch) && prev != 0) {
                    IntRef to = new IntRef(0);
                    if (processRange(to, pdata)) {
                        ret.addRange(prev, to.value);
                        prev = 0;
                        continue;
                    }
                }
                if (prev != 0) {
                    ret.addChar(prev);
                }
                if (Tools.isSetEnd(ch)) {
                    ret.unique();
                    return ret;
                }
                if (Tools.isSlash(ch)) {
                    Ret s = processSlash(false, pdata);
                    if (s.left != null) {
                        ret.addRange((Charset) s.left);
                        prev = 0;
                        continue;
                    }
                    ch = s.right;
                }
                prev = ch;
            }
            pdata.i_ = bak;
            return new Text('[');
        }

        NodeBase processGroup(ParseData pdata) {
            int bak = pdata.i_++;
            int mark = ignoreSubExpMarks(pdata);
            pdata.ends_.add(')');
            if (mark == 0) {
                mark = ++pdata.ref_;
            }
            Ret ret = processSeq(pdata);
            pdata.ends_.remove(pdata.ends_.size() - 1);
            if (ret.right != 0) {
                return new Group(ret.left, mark);
            }
            pdata.i_ = bak;
            return new Text('(');
        }

        Ret processSelect(NodeBase node, ParseData pdata) {
            Ret ret = new Ret(new Select(node), 0);
            pdata.ends_.add('|');
            for (int e = regx_.length(); pdata.i_ < e;) {
                ++pdata.i_;
                Ret r = processSeq(pdata);
                ret.left.appendNode(r.left);
                if (r.right > 1) {
                    ret.right = r.right - 1;
                    break;
                }
            }
            pdata.ends_.remove(pdata.ends_.size() - 1);
            return ret;
        }

        NodeBase processRepeat(NodeBase node, ParseData pdata) {
            if (node != null && node.repeat(0) != 0) {
                int bak = pdata.i_++;
                IntRef min = new IntRef(0), max = new IntRef(Repeat.INFINITE);
                switch (processInt(min, pdata)) {
                    case ',':
                        ++pdata.i_;
                        if (processInt(max, pdata) == '}') {
                            return new Repeat(node, min.value, (min.value < max.value ? max.value : min.value));
                        }
                        break;
                    case '}':
                        return new Repeat(node, min.value, min.value);
                    default:
                        ;
                }
                pdata.i_ = bak;
            }
            return new Text('{');
        }

        int processInt(IntRef result, ParseData pdata) {
            boolean begin = true;
            for (int e = regx_.length(); pdata.i_ < e; ++pdata.i_) {
                int ch = regx_.charAt(pdata.i_);
                if (Tools.isDigit(ch)) {
                    ch = Tools.transDigit(ch);
                    if (begin) {
                        result.value = ch;
                        begin = false;
                    } else {
                        result.value *= 10;
                        if (result.value < 0) {
                            return 0;
                        }
                        result.value += ch;
                    }
                } else {
                    return ch;
                }
            }
            return 0;
        }

        boolean processRange(IntRef result, ParseData pdata) {
            if (++pdata.i_ < regx_.length() && regx_.charAt(pdata.i_) != ']') {
                result.value = (int) regx_.charAt(pdata.i_);
                return true;
            }
            --pdata.i_;
            return true;
        }

        int ignoreSubExpMarks(ParseData pdata) {
            int ret = 0;
            if (pdata.i_ + 1 < regx_.length()) {
                ret = Tools.isSubExpMark(regx_.substring(pdata.i_));
                // ret = Tools.isSubExpMark(regx_.charAt(pdata.i_));
                if (ret != 0) {
                    pdata.i_ += regx_.indexOf(')') - pdata.i_;
                    // pdata.i_ += 2;
                }
            }
            return ret;
        }

        class IntRef {

            int value;

            public IntRef(int value){
                this.value = value;
            }
        }

    }

    static class Config {

        public static final int REPEAT_INFINITE = 3;
        private final int       repeatInfinite;

        public Config(){
            this.repeatInfinite = REPEAT_INFINITE;
        }

        public Config(final int repeatInfinite){
            this.repeatInfinite = repeatInfinite;
        }

        public int getRepeatInfinite() {
            return repeatInfinite;
        }

    }

    static class Edge extends NodeBase {

        private boolean begin_;

        public Edge(int ch){
            begin_ = ch == '^';
        }

        @Override
        public NodeBase optimize(ParseData pdata) {
            return REP_NULL;
        }

        @Override
        public void randString(GenerateData gdata) {
            System.out.println("__Edge");
        }

        public boolean isBegin() {
            return begin_;
        }
    }

    static class GenerateData {

        List<RefValue> refs_ = new ArrayList<RefValue>();

        StringBuffer   oss   = new StringBuffer();
    }

    static class Group extends NodeBase {

        public static int INDEX      = 1 << 16;
        public static int MAX_GROUPS = 9;
        int               mark_;
        NodeBase          node_;

        public Group(NodeBase node, int mark){
            this.node_ = node;
            this.mark_ = mark;
            if (Tools.isSubExpMark(mark) == 0) {
                mark_ |= INDEX;
            }
        }

        @Override
        public NodeBase optimize(ParseData pdata) {
            if (node_ == null || mark_ == '!') {
                return REP_NULL;
            }
            NodeBase r = node_.optimize(pdata);
            if (r == REP_NULL) {
                return REP_NULL;
            } else if (r != null) {
                node_ = r;
            }
            switch (mark_) {
                case ':':
                case '=':
                case '<':
                case '>': {
                    return node_;
                }
                default:
                    ;
            }
            mark_ = (mark_ & (INDEX - 1)) - 1;
            return null;
        }

        @Override
        public void randString(GenerateData gdata) {
            assert node_ != null;
            assert 0 <= mark_ && mark_ < MAX_GROUPS;
            gdata.refs_.add(new RefValue(gdata.oss.length(), -1));
            node_.randString(gdata);
            assert mark_ < gdata.refs_.size();
            gdata.refs_.get(mark_).right = gdata.oss.length() - gdata.refs_.get(mark_).left;
            // System.out.println("__Group");
        }

    }

    static abstract class NodeBase {

        public static NodeBase REP_NULL = new NullNodeBase();

        public abstract NodeBase optimize(ParseData pdata);

        public abstract void randString(GenerateData gdata);

        public int repeat(int ch) {
            return 1;
        }

        public void appendNode(NodeBase node) {
            throw new RuntimeException("should not at here!");
        }
    }

    static class NullNodeBase extends NodeBase {

        @Override
        public NodeBase optimize(ParseData pdata) {
            return null;
        }

        @Override
        public void randString(GenerateData gdata) {
        }

    }

    static class ParseData {

        List<Character> ends_ = new ArrayList<Character>();
        Config          config_;
        int             i_;
        int             ref_;

        public ParseData(Config config_){
            this.config_ = config_;
            this.i_ = 0;
            this.ref_ = 0;
        }

        public int inEnds(int ch) {
            int ret = 1;
            for (int i = ends_.size() - 1; i >= 0; i--, ++ret) {
                if (ch == ends_.get(i)) {
                    return ret;
                }
                if (Tools.needEnd(ends_.get(i))) {
                    break;
                }
            }
            return 0;
        }
    }

    static class Ref extends NodeBase {

        int index_;

        public Ref(int index){
            this.index_ = index;
        }

        @Override
        public NodeBase optimize(ParseData pdata) {
            --index_;
            return null;
        }

        @Override
        public void randString(GenerateData gdata) {
            assert index_ < gdata.refs_.size();
            RefValue ref = gdata.refs_.get(index_);
            String str = gdata.oss.toString();
            if (ref.left < str.length()) {
                gdata.oss.append(str.substring(ref.left, ref.right));
            }
            // System.out.println("__Ref(" + index_ + ")");
        }
    }

    static class RefValue {

        int left;
        int right;

        public RefValue(int left, int right){
            this.left = left;
            this.right = right;
        }

    }

    static class Repeat extends NodeBase {

        static int INFINITE     = 1 << 16;
        static int _REPEAT_MAX  = INFINITE - 1;
        static int _NON_GREEDY  = 1 << 17;
        static int _PROSSESSIVE = 1 << 18;
        static int _CLEAR_FLAGS = _NON_GREEDY - 1;
        NodeBase   node_;
        int        min_, max_;

        public Repeat(NodeBase node, int ch){
            this.node_ = node;
            min_ = 0;
            max_ = 0;
            switch (ch) {
                case '?':
                    min_ = 0;
                    max_ = 1;
                    break;
                case '+':
                    min_ = 1;
                    max_ = INFINITE;
                    break;
                case '*':
                    min_ = 0;
                    max_ = INFINITE;
                    break;
                default:
                    ;
            }
        }

        public Repeat(NodeBase node, int min, int max){
            this.node_ = node;
            this.min_ = min;
            this.max_ = max;
        }

        @Override
        public NodeBase optimize(ParseData pdata) {
            min_ &= _CLEAR_FLAGS;
            max_ &= _CLEAR_FLAGS;
            if (isInfinite()) {
                max_ = min_ + pdata.config_.getRepeatInfinite();
                if (max_ > _REPEAT_MAX) {
                    max_ = _REPEAT_MAX;
                }
            }
            if (node_ == null || (min_ > max_) || (min_ == 0 && max_ == 0)) {
                return REP_NULL;
            }
            NodeBase r = node_.optimize(pdata);
            if (r == REP_NULL) {
                return REP_NULL;
            } else if (r != null) {
                node_ = r;
            }
            if (max_ == 1 && min_ == 1) {
                return node_;
            }
            max_ -= min_ - 1;
            return null;
        }

        @Override
        public void randString(GenerateData gdata) {
            for (int t = min_ + Tools.rand() % max_; t > 0; t--) {
                node_.randString(gdata);
            }
            // System.out.println("__Repeat");
        }

        @Override
        public int repeat(int ch) {
            if (canRepeat()) {
                switch (ch) {
                    case '?':
                        min_ |= _NON_GREEDY;
                        return 2;
                    case '+':
                        min_ |= _PROSSESSIVE;
                        return 2;
                    default:
                        ;
                }
            }
            return 0;
        }

        boolean isInfinite() {
            return (max_ & INFINITE) != 0;
        }

        boolean isNonGreedy() {
            return (min_ & _NON_GREEDY) != 0;
        }

        boolean isPossessive() {
            return (min_ & _PROSSESSIVE) != 0;
        }

        boolean canRepeat() {
            return (min_ & (_NON_GREEDY | _PROSSESSIVE)) != 0;
        }

    }

    static class Ret {

        NodeBase left;
        int      right;

        public Ret(){

        }

        public Ret(NodeBase left, int right){
            this.left = left;
            this.right = right;
        }
    }

    static class Select extends NodeBase {

        List<NodeBase> sel_ = new ArrayList<NodeBase>();
        int            sz_;

        public Select(NodeBase node){
            sel_.add(node);
            sz_ = 0;
        }

        @Override
        public NodeBase optimize(ParseData pdata) {
            if (sel_.size() == 0) {
                return REP_NULL;
            }
            List<NodeBase> sel_n = new ArrayList<NodeBase>();
            for (Iterator<NodeBase> iter = sel_.iterator(); iter.hasNext();) {
                NodeBase i = iter.next();
                if (i != null) {
                    NodeBase r = i.optimize(pdata);
                    if (r != null) {
                        i = (r == REP_NULL ? null : r);
                    }
                }
                if (i != null) {
                    sel_n.add(i);
                }
            }
            sel_ = sel_n;
            if (sel_.size() == 0) {
                return REP_NULL;
            }
            if (sel_.size() == 1) {
                NodeBase r = sel_.get(0);
                sel_.clear();
                return r;
            }
            sz_ = sel_.size();
            return null;
        }

        @Override
        public void randString(GenerateData gdata) {
            if (sz_ != 0) {
                sel_.get(Tools.rand() % sz_).randString(gdata);
            }
            // System.out.println("__Select");
        }

        @Override
        public void appendNode(NodeBase node) {
            sel_.add(node);
        }

    }

    static class Seq extends NodeBase {

        List<NodeBase> seq_ = new ArrayList<NodeBase>();

        public Seq(NodeBase node){
            seq_.add(node);
        }

        @Override
        public NodeBase optimize(ParseData pdata) {
            if (seq_.size() == 0) {
                return REP_NULL;
            }
            List<NodeBase> seq_n = new ArrayList<NodeBase>();
            for (Iterator<NodeBase> iter = seq_.iterator(); iter.hasNext();) {
                NodeBase i = iter.next();
                if (i != null) {
                    NodeBase r = i.optimize(pdata);
                    if (r != null) {
                        i = (r == REP_NULL ? null : r);
                    }
                }
                if (i != null) {
                    seq_n.add(i);
                }
            }
            seq_ = seq_n;
            if (seq_.size() == 0) {
                return REP_NULL;
            }
            if (seq_.size() == 1) {
                NodeBase r = seq_.get(0);
                seq_.clear();
                return r;
            }
            return null;
        }

        @Override
        public void randString(GenerateData gdata) {
            for (NodeBase i : seq_) {
                i.randString(gdata);
            }
            // System.out.println("__Seq");
        }

        @Override
        public void appendNode(NodeBase node) {
            if (seq_.size() != 0) {
                if (node instanceof Text) {
                    Text cur = (Text) node;
                    NodeBase back = seq_.get(seq_.size() - 1);
                    if (back instanceof Text) {
                        Text prev = (Text) back;
                        prev.str_.append(cur.str_);
                        return;
                    }
                }
            }
            seq_.add(node);
        }

    }

    static class Text extends NodeBase {

        StringBuffer str_;

        public Text(int ch){
            str_ = new StringBuffer().append((char) ch);
        }

        @Override
        public NodeBase optimize(ParseData pdata) {
            return str_.length() == 0 ? REP_NULL : null;
        }

        @Override
        public void randString(GenerateData gdata) {
            gdata.oss.append(str_);
            // System.out.println("__Text");
        }
    }

    static class Tools {

        public static Random rand = new Random(System.currentTimeMillis());

        public static int rand() {
            return rand.nextInt(Integer.MAX_VALUE);
        }

        public static boolean isRepeat(int ch) {
            return ch == '?' || ch == '+' || ch == '*';
        }

        public static boolean isBegin(int ch) {
            return ch == '^';
        }

        public static boolean isEnd(int ch) {
            return ch == '$';
        }

        public static boolean isSlash(int ch) {
            return ch == '\\';
        }

        public static boolean isSetBegin(int ch) {
            return ch == '[';
        }

        public static boolean isSetEnd(int ch) {
            return ch == ']';
        }

        public static boolean isGroupBegin(int ch) {
            return ch == '(';
        }

        public static boolean isGroupEnd(int ch) {
            return ch == ')';
        }

        public static boolean isSelect(int ch) {
            return ch == '|';
        }

        public static boolean isRepeatBegin(int ch) {
            return ch == '{';
        }

        public static boolean isRepeatEnd(int ch) {
            return ch == '}';
        }

        public static boolean needEnd(int ch) {
            return isGroupEnd(ch) || isRepeatEnd(ch);
        }

        public static boolean isDigit(int ch) {
            return '0' <= ch && ch <= '9';
        }

        public static int transDigit(int ch) {
            return ch - '0';
        }

        public static boolean isDash(int ch) {
            return ch == '-';
        }

        public static boolean isAny(int ch) {
            return ch == '.';
        }

        public static int isSubExpMark(int ch) {
            // return ch == ':' || ch == '=' || ch == '!' || ch == '>' ? ch : 0;
            return ch == ':' || ch == '=' || ch == '!' || ch == '>' || ch == '<' ? ch : 0;
        }

        public static int isSubExpMark(String s) {
            return s.charAt(0) == '?' ? isSubExpMark(s.charAt(1)) : 0;
        }

        public static char transSlash(int ch) {
            switch (ch) {
                case 'f':
                    return '\f';
                case 'n':
                    return '\n';
                case 'r':
                    return '\r';
                case 't':
                    return '\t';
            }
            return (char) ch;
        }
    }

    static class Charset extends NodeBase {

        StringBuffer str_;
        int          inc_;

        public Charset(){
            inc_ = 1;
            this.str_ = new StringBuffer();
        }

        public Charset(String str_, boolean include){
            this.str_ = new StringBuffer().append(str_);
            this.inc_ = include ? 1 : 0;
        }

        public void exclude() {
            inc_ = 0;
        }

        public void addChar(int ch) {
            str_.append((char) ch);
        }

        public void addRange(int from, int to) {
            for (; from <= to; ++from) {
                str_.append((char) from);
            }
        }

        public void addRange(Charset node) {
            if (node == null) {
                return;
            }
            unite(node);
        }

        public void unique() {
            if (inc_ != 0) {
                unique_();
            } else {
                reverse();
            }
        }

        void unite(Charset node) {
            if (node.inc_ == 0) {
                node.reverse();
            }
            str_.append(node.str_);
        }

        void reverse() {
            if (inc_ != 0) {
                return;
            }
            int _CHAR_MIN = 32;
            int _CHAR_MAX = 126;
            unique_();
            StringBuffer s = new StringBuffer(str_.toString());
            str_ = new StringBuffer();
            int c = _CHAR_MIN;
            int i = 0, e = s.length();
            for (; c <= _CHAR_MAX && i < e; ++i) {
                int ch = s.charAt(i);
                if (c < ch) {
                    addRange(c, ch - 1);
                }
                c = Math.max(ch + 1, _CHAR_MIN);
            }
            if (c <= _CHAR_MAX) {
                addRange(c, _CHAR_MAX);
            }
            if (inc_ == 0) {
                inc_ = 1;
            } else {
                inc_ = 0;
            }
        }

        void unique_() {
            if (str_ != null && str_.length() != 0) {
                Set<Character> chars = new HashSet<Character>(str_.length());
                for (int i = 0; i < str_.length(); ++i) {
                    chars.add(str_.charAt(i));
                }
                List<Character> charlist = new ArrayList<Character>(chars);
                Collections.sort(charlist);
                str_ = new StringBuffer();
                for (Character ch : charlist) {
                    str_.append(ch);
                }
            }
        }

        @Override
        public NodeBase optimize(ParseData pdata) {
            if (inc_ != 0) {
                reverse();
            }
            if (str_.length() == 0) {
                return REP_NULL;
            }
            inc_ = str_.length();
            return null;
        }

        @Override
        public void randString(GenerateData gdata) {
            assert inc_ == str_.length();
            gdata.oss.append(str_.charAt(Tools.rand() % inc_));
            // System.out.println("__Charset");
        }

    }
}
