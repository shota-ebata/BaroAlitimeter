
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