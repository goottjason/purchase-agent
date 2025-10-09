/**
 * ------ 유틸리티/동작 함수 정의 ------
 */

// 1. 선택 개수 표시 + 버튼 활성화
function updateSelectedCount() {
  const count = $('.product-checkbox:checked').length;
  $('#selectedCount').text(count);
  $('#crawlAndPriceStockUpdateBtn').prop('disabled', count === 0);
}

// 2. "가격/재고 일괄 업데이트" 실행
function crawlAndPriceStockUpdate() {
  const selectedCodes = $('.product-checkbox:checked').map(function() {
    return $(this).val();
  }).get();

  if (selectedCodes.length === 0) return;

  // 입력값 읽기
  const marginRate = Number($('#margin-rate').val());
  const couponRate = Number($('#coupon-rate').val());
  const minMarginPrice = Number($('#min-margin-price').val());

  const params = {
    marginRate: marginRate,
    couponRate: couponRate,
    minMarginPrice: minMarginPrice
  };
  const qs = $.param(params);

  // 체크 상품 정보 추출
  const requests = selectedCodes.map(code => {
    const $row = $('input.product-checkbox[value="'+code+'"]').closest('tr');
    return {
      code: $row.data('code'),
      productDto: {
        code: $row.data('code'),
        supplierCode: $row.data('supplier-code'),
        title: $row.data('title'),
        link: $row.data('link'),
        unitValue: $row.data('unit-value'),
        unit: $row.data('unit'),
        packQty: $row.data('pack-qty'),
        salePrice: $row.data('sale-price'),
        stock: $row.data('stock'),
        korName: $row.data('kor-name'),
        engName: $row.data('eng-name'),
        brandName: $row.data('brand-name'),
        vendorItemId: $row.data('vendor-item-id'),
        sellerProductId: $row.data('seller-product-id'),
        smartstoreId: $row.data('smartstore-id'),
        originProductNo: $row.data('origin-product-no'),
        elevenstId: $row.data('elevenst-id'),
        cafeNo: $row.data('cafe-no'),
        cafeCode: $row.data('cafe-code'),
        cafeOptCode: $row.data('cafe-opt-code')
      },
      priceChanged: true,  // 혹은 실제 UI의 상태에 따라 true/false
      stockChanged: true   // 혹은 실제 UI의 상태에 따라 true/false
    };
  });

  $.ajax({
    url: '/products/crawl-and-price-stock-update?' + qs,
    method: 'POST',
    contentType: 'application/json',
    data: JSON.stringify(requests),
    success: function(resp) {
      alert('업데이트 요청이 성공적으로 MQ로 전송되었습니다.');
    },
    error: function(xhr) {
      alert('업데이트 요청 실패: ' + xhr.responseText);
    }
  });
}

// 3. 편집모드 진입
function enableEditing($cell) {
  const code = $cell.closest('tr').data('code');
  const field = $cell.data('field');
  const currentValue = $cell.data('value') || $cell.text().replace(/,/g, '');

  if ($cell.hasClass('editing')) return;
  $cell.addClass('editing');

  let inputProps = {
    class: 'editing-input',
    value: currentValue,
    'data-original': currentValue
  };

  if (field === 'salePrice') {
    // 가격은 100원 단위로만 step
    inputProps.type = 'number';
    inputProps.step = 100;
    inputProps.min = 0;
  } else if (field === 'stock') {
    // 재고는 1단위씩
    inputProps.type = 'number';
    inputProps.step = 1;
    inputProps.min = 0;
  } else {
    inputProps.type = 'text';
  }

  const $input = $('<input>', inputProps);

  $cell.html($input);
  $input.focus().select();

  $input.on('blur keypress', function(e) {
    if (e.type === 'blur' || e.which === 13) {
      finishEditing($cell, $input, code, field);
    }
  });

  $input.on('keydown', function(e) {
    if (e.which === 27) { // ESC
      cancelEditing($cell, $input);
    }
  });
}

// 4. 편집 완료
function finishEditing($cell, $input, code, field) {
  const newValue = $input.val();
  const originalValue = $input.data('original');
  if (field === 'salePrice' || field === 'stock') {
    if (isNaN(newValue) || newValue < 0) {
      alert('올바른 숫자를 입력해주세요.');
      $input.focus();
      return;
    }
  }
  $cell.removeClass('editing');
  if (newValue !== originalValue) {
    if (!modifiedData[code]) {
      modifiedData[code] = { code: code };
      modifiedData[code].sellerProductId = $cell.data('seller-product-id');
      modifiedData[code].vendorItemId = $cell.data('vendor-item-id');
      modifiedData[code].smartstoreId = $cell.data('smartstore-id');
      modifiedData[code].originProductNo = $cell.data('origin-product-no');
      modifiedData[code].elevenstId = $cell.data('elevenst-id');
    }
    modifiedData[code][field] = parseInt(newValue) || newValue;
    $cell.addClass('modified-cell');
    $('#manualPriceStockUpdateBtn').show();
  }
  const displayValue = field === 'salePrice' ? parseInt(newValue).toLocaleString() : newValue;
  $cell.text(displayValue).data('value', newValue);
}

// 5. 편집 취소
function cancelEditing($cell, $input) {
  const originalValue = $input.data('original');
  $cell.removeClass('editing');
  const displayValue = $cell.data('field') === 'salePrice' ? parseInt(originalValue).toLocaleString() : originalValue;
  $cell.text(displayValue);
}

// 6. 저장(일괄저장) 함수 (유저가 가격/재고 직접 수정)
function manualPriceStockUpdate() {

  const modifiedProductCount = Object.keys(modifiedData).length;
  if (modifiedProductCount === 0) {
    alert('저장할 변경사항이 없습니다.');
    return;
  }
  if (!confirm(`${modifiedProductCount}개 상품의 변경사항을 저장하시겠습니까?`)) {
    return;
  }

  $('#manualPriceStockUpdateBtn').prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> 저장 중...');

  // 실제 전송 데이터 구조 (DTO에 맞춰 명확!)
  const requests = Object.values(modifiedData).map(item => ({
    code: item.code,
    priceChanged: item.hasOwnProperty('salePrice'),
    stockChanged: item.hasOwnProperty('stock'),
    productDto: { // 반드시 productDto라는 필드로 묶을 것
      salePrice: item.salePrice !== undefined ? item.salePrice : null,
      stock: item.stock !== undefined ? item.stock : null,
      sellerProductId: item.sellerProductId,
      vendorItemId: item.vendorItemId,
      smartstoreId: item.smartstoreId,
      originProductNo: item.originProductNo,
      elevenstId: item.elevenstId
    }
  }));

  $.ajax({
    url: '/products/manual-price-stock-update',
    method: 'POST',
    contentType: 'application/json',
    data: JSON.stringify(requests), // 바로 배열로 보냄!
    success: function(response) {
      if (response.success) {
        showSaveIndicator('성공적으로 저장되었습니다.');
        modifiedData = {};
        $('.modified-cell').removeClass('modified-cell');
        $('#manualPriceStockUpdateBtn').hide();
      } else {
        alert('저장 실패: ' + response.message);
      }
    },
    error: function(xhr, status, error) {
      alert('저장 중 오류가 발생했습니다: ' + error);
    },
    complete: function() {
      $('#manualPriceStockUpdateBtn').prop('disabled', false).html('<i class="fas fa-save"></i> 변경사항 저장');
    }
  });
}


// 7. 저장 완료 안내
function showSaveIndicator(message) {
  $('#saveIndicator').text(message).fadeIn().delay(3000).fadeOut();
}

/**
 * ---- 체크박스·필터 함수 ----
 */
function filterSupplierOptions(searchText) {
  const options = $('#supplierFilterOptions .filter-option[data-value!="all"]');
  options.each(function() {
    const text = $(this).text().toLowerCase();
    if (text.includes(searchText.toLowerCase())) {
      $(this).show();
    } else {
      $(this).hide();
    }
  });
}

function applySupplierFilter() {
  const selectedSuppliers = [];
  $('.supplier-filter-checkbox:checked').each(function() {
    selectedSuppliers.push($(this).closest('.filter-option').data('value'));
  });
  $('#hiddenFilters input[name="supplierCodes"]').remove();
  selectedSuppliers.forEach(function(supplier) {
    $('#hiddenFilters').append(
      '<input type="hidden" name="supplierCodes" value="' + supplier + '">'
    );
  });
  const filterBtn = $('[data-filter="supplier"]');
  if (selectedSuppliers.length > 0) {
    filterBtn.addClass('filtered');
  } else {
    filterBtn.removeClass('filtered');
  }
  $('input[name="pageNumber"]').val(0);
  $('#searchForm').submit();
}

function resetSupplierFilter() {
  $('.supplier-filter-checkbox').prop('checked', false);
  $('#supplier-all').prop('checked', true);
  $('#hiddenFilters input[name="supplierCodes"]').remove();
  $('[data-filter="supplier"]').removeClass('filtered');
  $('input[name="pageNumber"]').val(0);
  $('#searchForm').submit();
}

function toggleNullFilter(channelType, isChecked) {
  $(`input[name="filterNull${channelType.charAt(0).toUpperCase() + channelType.slice(1)}"]`).val(isChecked);
  const filterBtn = $(`[data-filter="${channelType}"]`);
  if (isChecked) {
    filterBtn.addClass('filtered');
  } else {
    filterBtn.removeClass('filtered');
  }
  $('input[name="pageNumber"]').val(0);
  $('#searchForm').submit();
}

function goToPage(pageNumber) {
  $('input[name="pageNumber"]').val(pageNumber);
  $('#searchForm').submit();
}

function resetAllFilters() {
  $('#searchKeyword').val('');
  $('#pageSize').val('50');
  $('input[name="pageNumber"]').val('0');
  $('.supplier-filter-checkbox').prop('checked', false);
  $('#supplier-all').prop('checked', true);
  $('#hiddenFilters input[name="supplierCodes"]').remove();
  $('input[name="filterNullVendorItemId"]').val('false');
  $('input[name="filterNullSellerProductId"]').val('false');
  $('input[name="filterNullSmartstoreId"]').val('false');
  $('input[name="filterNullElevenstId"]').val('false');
  $('.filter-dropdown-btn').removeClass('filtered');
  $('#searchForm').submit();
}

function updateActiveFilters() {
  const filterTags = [];
  if (searchKeyword && searchKeyword !== 'null' && searchKeyword !== '') {
    filterTags.push(`검색: "${searchKeyword}"`);
  }
  if (selectedSuppliers && selectedSuppliers.length > 0) {
    filterTags.push(`공급업체: ${selectedSuppliers.join(', ')}`);
  }
  if (filterNullVendorItemId) filterTags.push('쿠팡 품목ID: NULL');
  if (filterNullSellerProductId) filterTags.push('쿠팡 상품ID: NULL');
  if (filterNullSmartstoreId) filterTags.push('스마트스토어ID: NULL');
  if (filterNullElevenstId) filterTags.push('11번가ID: NULL');
  console.log(filterTags);
  if (filterTags.length > 0) {
    let tagsHtml = '';
    filterTags.forEach(function(tag) {
      tagsHtml += `<span class="filter-tag">${tag} <span class="remove-filter" onclick="removeFilter('${tag}')">×</span></span>`;
    });
    $('#filterTags').html(tagsHtml);
    $('#activeFilters').show();
  } else {
    $('#activeFilters').hide();
  }
}

/**
 * ------ DOM 및 핸들러 등록 ------
 */
let modifiedData = {};
$(document).ready(function() {
  updateSelectedCount();

  // 전체 선택
  $('#selectAll').change(function() {
    $('.product-checkbox').prop('checked', $(this).prop('checked')).trigger('change');
  });

// 동적 체크박스 모두에 대응
  $(document).on('change', '.product-checkbox', function() {
    const totalCheckboxes = $('.product-checkbox').length;
    const checkedCheckboxes = $('.product-checkbox:checked').length;
    $('#selectAll').prop('checked', totalCheckboxes === checkedCheckboxes);
    $('#selectAll').prop('indeterminate', checkedCheckboxes > 0 && checkedCheckboxes < totalCheckboxes);
    updateSelectedCount();
  });


  // 일괄 가격/재고 업데이트
  $('#crawlAndPriceStockUpdateBtn').on('click', crawlAndPriceStockUpdate);

  // 셀 더블클릭 편집 진입
  $('.editable-cell').dblclick(function() {
    enableEditing($(this));
  });

  // 저장 버튼
  $('#manualPriceStockUpdateBtn').click(manualPriceStockUpdate);

  // 필터 관련 (공급업체 등)
  updateActiveFilters();

  // 페이지 사이즈/정렬 변경
  $('#pageSize').change(function() {
    $('#searchForm').submit();
  });
  $('#sortOrder').change(function() {
    $('#searchForm').submit();
  });

});
