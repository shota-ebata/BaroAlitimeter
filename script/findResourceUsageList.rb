
class FileNameWithLines
  attr_accessor :full_file_name, :lines

  def initialize(full_file_name, lines)
    @full_file_name = full_file_name
    @lines = lines
  end
end

def get_line_number_list(full_file_name, search_text)
    hit_lines = []
    return [] unless File.file?(full_file_name)

    File.open(full_file_name, "r") do |file|
        line_number = 1
        file.each_line do |line|
            if line.include?(search_text)
                hit_lines.append(line_number)
            end
            line_number += 1
        end
    end
    return hit_lines
end

def find_file_names_include(search_text)
    # 特定のテキストを含むファイルの名前を格納する配列を初期化
    hit_file_name_list = []

    # 指定したディレクトリ内のファイルを走査して特定のテキストを含むファイルを検索
    Dir.glob("**/*").each do |full_file_name|
        next if full_file_name.include?("build/")
        next unless File.file?(full_file_name)

        # ファイルを開いてテキストを検索
        if File.read(full_file_name).include?(search_text)
            hit_lines = get_line_number_list(full_file_name, search_text)
            hit_file_name_list.append(FileNameWithLines.new(full_file_name, hit_lines))
        end
    end
    return hit_file_name_list
end

def find_string_res_usage_file_name_list(string_res_name)
    res_use_file_name_list1 = find_file_names_include("R.string.#{string_res_name}")
    res_use_file_name_list2 = find_file_names_include("@string/#{string_res_name}")
    return res_use_file_name_list1 + res_use_file_name_list2
end

# 差分から追加行だけを抽出
def get_additional_row_list(diff_lines)
    additional_row_list = []
    diff_lines.each do |line|
        # 差分から追加行だけを抽出
        if line.match(/^\+{1}[ ].+/)
            additional_row_list.append(line.sub("+ ", ""))
        end
    end
    return additional_row_list
end

# <xxx name="リソース名">リソース</xxx>形式のテキストからリソース名を抽出
def get_resource_name(text)
    # <xxx name="リソース名">リソース</>形式のテキストだけを抽出する
    match = text.match(/<.+ name=".+">.+<\/.+>/)
    return nil if !match
    res_text = match[0]
    # リソース名取得
    return res_text.sub(/<.+ name="/, "").sub(/">.+<\/.+>/, "")
end

# Stringリソース使用箇所一覧メッセージを作成
def create_string_res_usage_list_message(diff_lines:)
    additional_row_list = get_additional_row_list(diff_lines)
    message_text = ""
    additional_row_list.each do |additional_row_text|
        # リソース名取得
        string_res_name = get_resource_name(additional_row_text)
        # リソース名を出力に加える
        message_text += "- `" + string_res_name + "`\n"
        # Stringリソース使用ファイル一覧を取得
        hit_file_name_list = find_string_res_usage_file_name_list(string_res_name)
        # ファイル一覧も出力に加える
        message_text += hit_file_name_list.map { |hit_file_name| "  - #{hit_file_name.full_file_name} :#{hit_file_name.lines}\n" }.join
    end
    return message_text
end

# ファイル名(aaa/bbb/xxx.xml, aaa/bbb/xxx.png)から名前部分(xxx)だけを抽出
def get_name_by_full_file_name(full_file_name:)
    match = full_file_name.match(/\w+\..+$/)
    return match[0].sub("/", "").sub(/\..+$/, "")
end

def find_drawable_res_usage_file_name_list(drawable_res_name:)
    res_use_file_name_list1 = find_file_names_include("R.drawable.#{drawable_res_name}")
    res_use_file_name_list2 = find_file_names_include("@drawable/#{drawable_res_name}")
    return res_use_file_name_list1 + res_use_file_name_list2
end

# リソース使用箇所の一覧を表示する
def show_res_usage_message(git)
    # Pull Request内のファイル変更を取得
    changed_files = git.modified_files + git.added_files

    # Stringリソースの変更をチェック
    strings_xml_file_name_list = changed_files.filter_map { |full_file_name| full_file_name if full_file_name.include?("res/values/strings.xml") }
    strings_xml_file_name_list.each do |full_file_name|
        # 変更行の一覧を取得
        diff = git.diff_for_file(full_file_name)
        # 変更行がある場合にのみコメントを出力
        if diff
            message_text = "<b>Stringリソース(#{full_file_name})の影響範囲</b>\n"
            message_text += create_string_res_usage_list_message(diff_lines: diff.patch.lines)
            # danger出力
            message(message_text)
        end
    end
    # Drawableリソースの変更をチェック
    drawable_message_text = "<b>Drawableリソースの影響範囲</b>\n"
    drawable_file_name_list = changed_files.filter_map { |full_file_name| full_file_name if full_file_name.include?("res/drawable") }
    drawable_file_name_list.each do |res_full_file_name|
        # リソース名だけを抽出
        res_name = get_name_by_full_file_name(full_file_name: res_full_file_name)
        drawable_message_text += "- `#{res_full_file_name}`\n"
        # リソース使用しているファイル一覧を取得する
        hit_file_name_list = find_drawable_res_usage_file_name_list(drawable_res_name: res_name)
        hit_file_name_list.each do |hit_file_name|
            drawable_message_text += "  - #{hit_file_name.full_file_name} :#{hit_file_name.lines}\n"
        end
    end
    # danger出力
    message(drawable_message_text)
end