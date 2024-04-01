
def get_line_number(file, search_text)
    line_number = 1
    file.each_line do |line|
        if line.include?(search_text)
            return line_number
        end
        line_number += 1
    end
    return -1
end


def find_file_names_include(search_text)
   # 検索するディレクトリを指定
   search_directory = "app/src"

   # 特定のテキストを含むファイルの名前を格納する配列を初期化
   files_with_text = []

   # 指定したディレクトリ内のファイルを走査して特定のテキストを含むファイルを検索
   Dir.glob("#{search_directory}/**/*").each do |file_name|
     next unless File.file?(file_name)

     # ファイルを開いてテキストを検索
     if File.read(file_name).include?(search_text)
       files_with_text << file_name
     end
   end
   return files_with_text
end


def get_string_res_usage_file_list(res_text)
    string_res_name = res_text.sub(/<.+ name="/, "").sub(/">.+<\/.+>/, "")
    res_use_file_name_list1 = find_file_names_include("R.string.#{string_res_name}")
    res_use_file_name_list2 = find_file_names_include("@string/#{string_res_name}")

    message_text_list = []
    message_text_list << "- `" + res_text
    message_text_list << res_use_file_name_list1.unshift("  - ") if !res_use_file_name_list1.empty?
    message_text_list << res_use_file_name_list2.unshift("  - ") if !res_use_file_name_list2.empty?

    return message_text_list.push("\n").join
end