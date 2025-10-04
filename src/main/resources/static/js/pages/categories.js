// ---------- 계층형 트리 테이블 렌더링(DFS) ----------
/**
 * nodes 배열(List<CategoryTreeDto>)을 테이블 형태로 렌더링
 * 계층 구조를 재귀적으로 만들어 각 행에 트리구조, 버튼, 링크 등을 표시
 * @param nodes 계층 트리 구조 데이터 배열
 */
function renderTreeTable(nodes) {
  const tbody = $('#category-tree-table').empty();
  if(nodes.length === 0){
    $('#no-data-message').show();
    return;
  }
  $('#no-data-message').hide();

  // 재귀적으로 각 노드를 렌더링하는 헬퍼 함수
  function renderNode(node, level, parentId) {
    const isExpandable = (node.children && node.children.length);
    const rowId = `cat-${node.id}`;
    // parentRow: 자식에게 부모 CSS 클래스 및 display:none 설정
    const parentRow = parentId ? ` class="tree-child of-${parentId}" style="display:none;"` : "";
    const caretClass = isExpandable ? "caret-right tree-toggle" : "";

    tbody.append(`
      <tr id="${rowId}" data-id="${node.id}" data-parent="${parentId || ''}" data-level="${level}"${parentRow}>
        <td>
          <span class="tree-indent" style="--level:${level};"></span>
          ${isExpandable ? `<span class="${caretClass}"></span>` : ''}
          <span>${node.name}</span>
        </td>
        <td class="text-center">
          ${node.link ? `<a href="${node.link}" target="_blank" class="btn btn-outline-info btn-sm">바로가기</a>` : '-'}
        </td>
        <td class="text-center">
          <button class="btn btn-primary btn-sm edit-tree-btn" data-id="${node.id}" data-name="${node.name}" data-link="${node.link || ''}">수정</button>
          <button class="btn btn-danger btn-sm delete-tree-btn" data-id="${node.id}">삭제</button>
        </td>
      </tr>
    `);
    // 자식 노드가 있을 경우 재귀적으로 렌더링
    if(isExpandable){
      node.children.forEach(child => renderNode(child, level+1, node.id));
    }
  }
  // 루트 노드부터 일괄 렌더링 시작
  nodes.forEach(n => renderNode(n, 0));
}

/**
 * 카테고리 트리 데이터를 서버에서 조회하여 테이블로 렌더링하는 함수
 */
function loadCategoryTree() {
  $('#loading-spinner').show(); // 로딩 스피너 표시

  $.ajax({
    url: '/categories/tree', // 계층 펼침 JSON 포맷 endpoint
    type: 'GET',
    data: { keyword: $('#search-input').val().trim() }, // 검색어 파라미터 전달
    success: function(res) {
      // List<CategoryTreeDto> 반환 후 테이블 렌더
      renderTreeTable(res || []);
    },
    error: function(){ alert('목록 로드 실패'); },
    complete: function(){ $('#loading-spinner').hide();}
  });
}

$(function(){

  // ---------- 페이지 최초 트리 로딩 ----------
  loadCategoryTree();

  // ---------- 트리 확장/축소(토글) ----------
  $('#category-tree-table').on('click','.tree-toggle',function(){
    // 클릭된 caret(화살표)
    const $caret = $(this),
      $tr = $caret.closest('tr'),
      currId = $tr.data('id');
    const open = $caret.hasClass('caret-down'); // 현재 열림 상태
    $caret.toggleClass('caret-down caret-right');
    // 자식 TR show/hide 수행(재귀)
    toggleChildren(currId, !open);

    /**
     * 계층 자식 노드 show/hide + 하위 전체 닫기
     * @param pid 부모 행의 ID
     * @param open true: 표시, false: 숨김
     */
    function toggleChildren(pid, open){
      $(`#category-tree-table tr.of-${pid}`).each(function(){
        $(this).toggle(open);
        // 부모 닫힘 상태면 하위 트리도 모두 닫기(캐럿 재설정)
        if(!open) $(this).find('.tree-toggle').removeClass('caret-down').addClass('caret-right');
        // 열림 상태에서 자식도 모두 열려 있으면 재귀적으로 열기
        if(open && $(this).find('.tree-toggle').hasClass('caret-down'))
          toggleChildren($(this).data('id'), true);
        else if(!open)
          toggleChildren($(this).data('id'), false);
      });
    }
  });

  // ---------- 검색/초기화 버튼 ----------
  $('#search-form').submit(function(e){
    e.preventDefault();
    loadCategoryTree(); // 검색 시 트리 목록 로딩
  });
  $('#reset-button').click(function(){
    $('#search-input').val('');
    loadCategoryTree(); // 검색어 초기화 후 트리 목록 로딩
  });

  // ---------- 수정 버튼 클릭 이벤트 ----------
  $('#category-tree-table').on('click', '.edit-tree-btn', function(){
    // 수정 모달에 카테고리 정보 데이터 채우기
    $('#edit-category-id').val($(this).data('id'));
    $('#edit-category-name').val($(this).closest('tr').find('span:last').text());
    $('#edit-link').val($(this).data('link'));
    $('#categoryEditModal').modal('show'); // 모달 오픈
  });

  // ---------- 수정 완료 폼(submit) 이벤트 ----------
  $('#category-edit-form').submit(function(e){
    e.preventDefault();
    $.ajax({
      url:'/admin/categories/'+$('#edit-category-id').val(),
      type:'PUT',
      contentType:'application/json',
      data:JSON.stringify({
        name: $('#edit-category-name').val().trim(),
        link: $('#edit-link').val().trim()
      }),
      success:function(){
        alert('수정완료');
        $('#categoryEditModal').modal('hide');
        loadCategoryTree(); // 수정 후 트리 재로딩
      },
      error:function(){
        // 에러 발생 시 서버 메시지(alert) 또는 '수정 실패' 알림
        alert((x.responseJSON && x.responseJSON.message) || '수정 실패');
      }
    });
  });

  // ---------- 삭제 버튼 클릭 ----------
  $('#category-tree-table').on('click','.delete-tree-btn',function(){
    if(confirm('정말 삭제하시겠습니까?')){
      $.ajax({
        url:'/admin/categories/'+$(this).data('id'),
        type:'DELETE',
        success:function(){
          alert('삭제완료');
          loadCategoryTree(); // 삭제 후 재로딩
        },
        error:function(){
          // 에러 발생 시 서버 메시지(alert) 또는 '등록 실패' 알림
          alert((x.responseJSON && x.responseJSON.message) || '삭제 실패');
        }
      });
    }
  });

  // ---------- 등록 폼(submit) 이벤트 ----------
  $('#category-simple-form').submit(function(e) {
    e.preventDefault();

    const formData = {
      categoryPath: $('[name=categoryPath]').val().trim(),
      link: $('[name=link]').val().trim()
    };

    $.ajax({
      url: '/admin/categories/create',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(formData),
      success: function (response) {
        // 서버에서 성공 응답 메시지 표시
        if (response.success) {
          alert(response.message);
          $('#category-simple-form')[0].reset();
          loadCategoryTree(); // 등록 후 재로딩
        } else {
          // 서버에서 success=false + message로 응답
          alert(response.message || '등록 실패');
        }
      },
      error: function (xhr) {
        // 에러 발생 시 서버 메시지(alert) 또는 '등록 실패' 알림
        alert((x.responseJSON && x.responseJSON.message) || '등록 실패');
      }
    });
  });

  // ---------- 엑셀 업로드용 파일선택 이벤트 ----------
  $('#excelFileInput').change(function () {
    // 파일 이름 표시에 사용
    const fileName = this.files && this.files.length ? this.files[0].name : '선택된 파일 없음';
    $('#excelFileName').text(fileName);
  });

  // ---------- 엑셀 업로드 폼(submit) ----------
  $('#excel-upload-form').submit(function (e) {
    e.preventDefault();
    const formData = new FormData(this); // 파일포함 폼 객체 생성
    $.ajax({
      url: '/admin/categories/upload-excel',
      type: 'POST',
      data: formData,
      processData: false, // 파일 데이터 그대로 전송
      contentType: false, // 헤더 자동설정(멀티파트)
      success: function (response) {
        alert(response.message || '엑셀 등록 완료');
        $('#excel-upload-form')[0].reset();
        $('#excelFileName').text('선택된 파일 없음'); // 파일명 초기화
        loadCategoryTree(); // 업로드 후 트리 재로딩
      },
      error: function (xhr) {
        // 에러 발생 시 서버 메시지(alert) 또는 '등록 실패' 알림
        alert((x.responseJSON && x.responseJSON.message) || '등록 실패');
      }
    });
  });

});
