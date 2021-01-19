Ext.define('app.view.fileMoveManage.mgmapfigureList', {
    extend: 'Ext.grid.Panel',
    xtype: 'view-fileMoveManage-mgmapfigureList',
    requires: [],
    padding: "10px 20px 0 20px",
    config: {
        tabtitle: ''
    },
    constructor: function (config) {
        var me = this;
        config = config || {};
        Ext.applyIf(config, me.config);
        this.callParent(arguments);
    },
    initComponent: function () {
        var me = this;
        var newstore = Ext.create('Ext.data.Store', {
            id: 'simpsonsStore',
            autoLoad: true,
            pageSize: 20, // 每页的条目数量
            proxy: {
                type: 'ajax',
                url: 'MgMapFigure/MgMapFigureList',
                reader: {
                    type: 'json',
                    rootProperty: 'rows',
                    totalProperty: "total"
                }
            }
        });
        Ext.apply(me, {
            tbar: {
                layout: 'column',
                scope: me,
                items: [
                    {
                        xtype: 'toolbar',
                        columnWidth: 1,
                        scope: me,
                        itemId: 'search',
                        items: [
                            {
                                xtype: 'button',
                                text: '下载',
                                scope: me,
                                itemId: 'bt_downLoad',
                                handler: function () {
                                    //判断是否选择额一条数据。
                                    var selectData = me.getSelectionModel().getSelection();
                                    if (selectData.length != 1) {
                                        Ext.Msg.alert('温馨提示', '请选择一条操作数据');
                                        return;
                                    }
                                    var id = selectData[0].get('id');
                                    var dataSource = selectData[0].get('dataSource');
                                    window.open('/filemove/MgMapFigure/download?id=' + id +'&'+'dataSource='+dataSource);
                                }
                            },
                            '->',
                            {
                                xtype: 'combobox',
                                emptyText: '请选择数据源',
                                store: {
                                    fields: ['name'],
                                    autoLoad: true,
                                    data: [
                                        {'name': '中心城区', 'value': 'cd'},
                                        {'name': '天府新区', 'value': 'tfxq'},
                                        {'name': '龙泉驿', 'value': 'lq'},
                                        {'name': '双流', 'value': 'sl'},
                                        {'name': '青白江', 'value': 'qbj'},
                                        {'name': '温江', 'value': 'wj'},
                                        {'name': '郫县', 'value': 'px'},
                                        {'name': '新都', 'value': 'xd'},
                                        {'name': '简阳', 'value': 'jy'},
                                        {'name': '金堂', 'value': 'jt'},
                                        {'name': '大邑', 'value': 'dy'},
                                        {'name': '浦江', 'value': 'pj'},
                                        {'name': '新津', 'value': 'xj'},
                                        {'name': '都江堰', 'value': 'djy'},
                                        {'name': '彭州', 'value': 'pz'},
                                        {'name': '邛崃', 'value': 'ql'},
                                        {'name': '崇州', 'value': 'cz'}
                                    ]
                                },
                                editable: false,
                                displayField: 'name',
                                valueField: 'value',
                                labelWidth: 60,
                                width: 150,
                                itemId: 'dataSourceId',
                                listeners: {
                                    "afterRender": function (combo) {
                                        var firstValue = combo.getStore().getAt(0);
                                        combo.setValue(firstValue);
                                    }
                                }
                            },
                            {
                                xtype: 'combobox',
                                emptyText: '请选择查询条件',
                                store: {
                                    fields: ['name'],
                                    autoLoad: true,
                                    data: [
                                        {'name': '业务件id', 'value': 'id'},
                                        {'name': '成果id', 'value': 'resultsid'},
                                    ]
                                },
                                editable: false,
                                displayField: 'name',
                                valueField: 'value',
                                labelWidth: 60,
                                width: 150,
                                itemId: 'searchCom'
                            },
                            {
                                xtype: 'textfield',
                                emptyText: '请输入',
                                labelWidth: 60,
                                width: 200,
                                itemId: 'searchContent'
                            },
                            {
                                xtype: 'button', text: '搜索', scope: me, glyph: 'xf002@FontAwesome',
                                handler: function () {
                                    var dataSource = me.queryById("dataSourceId").getValue();
                                    var searchCom = me.queryById("searchCom").getValue();
                                    var searchContent = me.queryById("searchContent").getValue();

                                    var params = {};
                                    if (searchCom!=null && searchCom!=''
                                    && searchContent!=null && searchContent!=''){
                                        params[searchCom] = searchContent;
                                    }
                                    if (dataSource!=null && dataSource!=''){
                                        params.dataSource = dataSource;
                                    }
                                    me.store.reload({
                                        params :params
                                    });
                                }
                            },
                            {
                                xtype: 'button', text: '重置', scope: me, glyph: 'xf0e2@FontAwesome',
                                handler: function () {
                                    me.queryById("searchCom").setValue(null);
                                    me.queryById("searchContent").setValue(null);

                                    var dataSourceCombo=me.queryById("dataSourceId");
                                    var firstValue = dataSourceCombo.getStore().getAt(0);
                                    dataSourceCombo.setValue(firstValue);
                                }
                            }
                        ]
                    }
                ]
            },
            border: true,
            store: newstore,
            title: me.config.tabtitle,
            // columnLines: true,
            // closable: true,
            viewConfig: {
                enableTextSelection: true
            },
            columns: [
                {text: 'id', dataIndex: 'id', width: '9%', align: 'center'},
                {text: '成果id', dataIndex: 'resultsid', width: '9%', align: 'center'},
                {text: '文件名称', dataIndex: 'imagename', width: '15%', align: 'center'},
                {text: '文件类型', dataIndex: 'imagetype', width: '15%', align: 'center'},
                {text: '文件状态', dataIndex: 'mgstatus', width: '9%', align: 'center'},
                {text: '文件后缀', dataIndex: 'imgstyle', width: '10%', align: 'center'},
                {text: '文件大小/byte', dataIndex: 'imgfilesize', width: '9%', align: 'center'},
                {text: '上传时间', dataIndex: 'regidate', width: '15%', align: 'center'},
                {text: '上传人', dataIndex: 'creater', width: '9%', align: 'center'}
            ],
            listeners: {
                celldblclick: function (cmp, td, cellndex, record, tr, rowindex, e, eopts) {
                    me.infoWindow('view', record);
                }
            },
            dockedItems: [
                {
                    xtype: 'pagingtoolbar',
                    dock: 'bottom',
                    displayInfo: true,
                    store: newstore
                }
            ]


        })
        ;
        me.callParent(arguments);
    },

    //详情窗口
    infoWindow: function (operation, selectData) {
        // window.open("http://www.jb51.net");
        var me = this;
        var result;
        Ext.Ajax.request({
            url: 'newsMg/findNewsInfo',
            params: {'id': selectData.get('id')},
            method: 'POST',
            async: false,
            success: function (response, options) {

                var response = JSON.parse(response.responseText);
                if (response.success) {
                    result = response.result;
                    // Ext.getCmp('newsInfo').getForm().setValues(result);
                } else {
                    Ext.Msg.alert("系统提示", response.message);
                }

            },
            failure: function (response) {
                Ext.Msg.alert("系统提示", "请求超时");
            }
        });

        Ext.create("Ext.window.Window", {
            title: result['title'],
            modal: true,
            layout: 'fit',
            width: '75%',
            height: '75%',
            scrollable: 'y',
            resizable: false,
            padding: '10 10 10 10',
            html: result['details'],
            fbar: [
                '->',
                {
                    xtype: 'button',
                    text: "关闭",
                    handler: function (btn) {
                        var win = btn.up().up();
                        win.close();
                    }
                }
            ]
        }).show();
    },


});
