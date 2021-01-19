Ext.define('app.view.fileMoveManage.moveRecordList', {
    extend: 'Ext.grid.Panel',
    xtype: 'view-fileMoveManage-moveRecordList',
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
                url: 'fileMoveRecord/fileMoveRecordList',
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
                            // {
                            //     xtype: 'button',
                            //     text: '下载',
                            //     scope: me,
                            //     itemId: 'bt_downLoad',
                            //     handler: function () {
                            //         //判断是否选择额一条数据。
                            //         var selectData = me.getSelectionModel().getSelection();
                            //         if (selectData.length != 1) {
                            //             Ext.Msg.alert('温馨提示', '请选择一条操作数据');
                            //             return;
                            //         }
                            //         var fileUuid = selectData[0].get('uuid');
                            //         window.open('/filemove/fastFile/download?fileUuid=' + fileUuid );
                            //     }
                            // },
                            '->',
                            {
                                xtype: 'combobox',
                                emptyText: '请选择数据源',
                                store: {
                                    fields: ['name'],
                                    autoLoad: true,
                                    data: [
                                        {'name': '中心城区', 'value': '510100'},
                                        {'name': '天府新区', 'value': '510142'},
                                        {'name': '龙泉驿', 'value': '510112'},
                                        {'name': '双流', 'value': '510122'},
                                        {'name': '青白江', 'value': '510113'},
                                        {'name': '温江', 'value': '510115'},
                                        {'name': '郫县', 'value': '510124'},
                                        {'name': '新都', 'value': '510114'},
                                        {'name': '简阳', 'value': '510180'},
                                        {'name': '金堂', 'value': '510121'},
                                        {'name': '大邑', 'value': '510129'},
                                        {'name': '浦江', 'value': '510131'},
                                        {'name': '新津', 'value': '510132'},
                                        {'name': '都江堰', 'value': '510181'},
                                        {'name': '彭州', 'value': '510182'},
                                        {'name': '邛崃', 'value': '510183'},
                                        {'name': '崇州', 'value': '510184'}
                                    ]
                                },
                                editable: false,
                                displayField: 'name',
                                valueField: 'value',
                                labelWidth: 60,
                                width: 150,
                                itemId: 'dataSourceId'
                            },
                            {
                                xtype: 'combobox',
                                emptyText: '请选择数据表',
                                store: {
                                    fields: ['name'],
                                    autoLoad: true,
                                    data: [
                                        {'name': 'mgmapfigure', 'value': 'mgmapfigure'},
                                        {'name': 'mgdoorimg', 'value': 'mgdoorimg'},
                                        {'name': 'imgimages', 'value': 'imgimages'}
                                    ]
                                },
                                editable: false,
                                displayField: 'name',
                                valueField: 'value',
                                labelWidth: 60,
                                width: 150,
                                itemId: 'tableNameId'
                            },
                            {
                                xtype: 'combobox',
                                emptyText: '请选择任务状态',
                                store: {
                                    fields: ['name'],
                                    autoLoad: true,
                                    data: [
                                        {'name': '未迁移', 'value': '未迁移'},
                                        {'name': '迁移成功', 'value': '迁移成功'},
                                        {'name': '迁移失败', 'value': '迁移失败'},
                                        {'name': '不迁移', 'value': '不迁移'},
                                    ]
                                },
                                editable: false,
                                displayField: 'name',
                                valueField: 'value',
                                labelWidth: 60,
                                width: 150,
                                itemId: 'moveStatusId'
                            },
                            {
                                xtype: 'combobox',
                                emptyText: '请选择查询条件',
                                store: {
                                    fields: ['name'],
                                    autoLoad: true,
                                    data: [
                                        {'name': '文件uuid', 'value': 'uuid'},
                                        {'name': '业务件uuid', 'value': 'businessUuid'},
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
                                    var tableName = me.queryById("tableNameId").getValue();
                                    var moveStatus = me.queryById("moveStatusId").getValue();
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
                                    if (tableName!=null && tableName!=''){
                                        params.tableName = tableName;
                                    }
                                    if(moveStatus!=null && moveStatus!=''){
                                        params.moveStatus = moveStatus;
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
                                    me.queryById("dataSourceId").setValue(null);
                                    me.queryById("tableNameId").setValue(null);
                                    me.queryById("moveStatusId").setValue(null);
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
                {text: '任务uuid', dataIndex: 'uuid', width: '9%', align: 'center'},
                {text: '业务id（bizid）', dataIndex: 'bizid', width: '9%', align: 'center'},
                {text: '状态', dataIndex: 'moveStatus', width: '9%', align: 'center'},
                {text: '备注', dataIndex: 'remark', width: '9%', align: 'center'},
                {text: '迁移线程', dataIndex: 'threadId', width: '9%', align: 'center'},
                {text: '文件uuid', dataIndex: 'fileUuid', width: '10%', align: 'center'},
                {text: 'storeid', dataIndex: 'fileStoreId', width: '9%', align: 'center'},
                {text: '文件大小/byte', dataIndex: 'fileSize', width: '9%', align: 'center'},
                {text: '迁移时间', dataIndex: 'createtime', width: '9%', align: 'center'},
                {text: '数据源', dataIndex: 'dataSource', width: '9%', align: 'center'},
                {text: '数据来源表', dataIndex: 'tablename', width: '9%', align: 'center'},
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
